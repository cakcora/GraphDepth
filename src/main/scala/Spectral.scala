import java.io.FileWriter

import breeze.linalg.normalize
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import ml.sparkling.graph.api.operators.measures.VertexMeasureConfiguration
import org.apache.spark.graphx._
import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.sql.SparkSession
import ml.sparkling.graph.operators.OperatorsDSL._
import org.apache.spark.graphx.lib.org.trustedanalytics.sparktk.BetweennessCentrality

import scala.collection.immutable.ListMap
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
/**
  * Created by cxa123230 on 3/29/2017.
  * https://gist.github.com/vrilleup/9e0613175fab101ac7cd
  */
object Spectral {
  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
  Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR)

  val spark = SparkSession
    .builder
    .appName("GraphDepth")
    .master("local[16]")
    .getOrCreate()
  Logger.getRootLogger().setLevel(Level.ERROR)
  val sc = spark.sparkContext


  def main(args: Array[String]): Unit = {

    val nameList = List("facebook-ego","CA-GrQc", "wiki-vote", "CA-AstroPh", "Ca-CondMat", "CA-HepTh", "enron", "kite", "web-stanford","web-notredame", "amazon", "web-berkeley", "web-google","dblp",  "youtube")
    val dir = "C:/Users/cxa123230/Dropbox/Publications/PostDoc work/Data Depth/results/"
    for (networkName <- nameList) {
      val resultFile = new FileWriter(dir+networkName+"AdjacencyResult.txt")
      val g: Graph[Int, Int] = DataLoader.load(sc, networkName, Map(("vertices" -> 34))) //
      for (method <- List("degree")) {
        var graph: Graph[Int, Int] = GraphCleaning.makeUndirected((g))
        resultFile.append(s"The $networkName network."+"\r\n")
        println(s"The $networkName network."+"\r\n")
//        println(graph.numVertices + " vertices, " + graph.numEdges + " edges.")
        var iteration = 0
        val initialK = (graph.numVertices / 100).toInt
        while (graph.numEdges > 0) {
          //iterate(graph,iteration,method=method)
          val ops = new MatrixOps()
          val mat: RowMatrix = ops.createMatrix(sc, graph, laplacian = false)

          val eigenCount: Int = if (graph.numVertices > 100) 30 else mat.numCols().toInt
          val svd = mat.computeSVD(eigenCount, computeU = true)
          val U: RowMatrix = svd.U
          val s: Vector = svd.s
          val V: Matrix = svd.V
          resultFile.append(method + "\t" + iteration + "\t" + graph.numVertices + "\t" + graph.numEdges + "\t" + s.toArray.mkString("\t")+"\r\n")
          resultFile.flush()


          val order = if (method == "closeness" || method == "depth") 1 else -1
          val influence: Map[VertexId, Double] = prominenceValues(graph, method)
          val k = if (graph.numVertices > 100) initialK else 1

          //choose k candidates to remove
          val sortedValues: ListMap[VertexId, Double] = ListMap(influence.toSeq.sortBy(order * _._2): _*)
          //println(sortedValues.mkString(","))
          val li = new scala.collection.mutable.ArrayBuffer[Long]
          val it = sortedValues.iterator
          for (i <- 1 to k) {
            if (it.hasNext) {
              val n1 = it.next()._1
              li.append(n1)
            }
          }
          //       println("Removing "+li.mkString(" and "))
          val newEdges = graph.edges.filter(f => !li.contains(f.srcId) && !li.contains(f.dstId))

          graph = Graph.fromEdges(newEdges, defaultValue = 0)
          iteration += 1
        }
      }
      resultFile.close()
    }
  }

  def prominenceValues(graph: Graph[PartitionID, PartitionID], method: String): Map[VertexId, Double] = {
    val d = new Centrality()
    method match {
      case "betweenness" => BetweennessCentrality.run(graph,normalize=true)
      case "degree" =>d.degree(graph)
      case "closeness" => d.closeness(graph,depth=false)
      case "depth" => d.closeness(graph,depth=true)
      case "pagerank" => d.pageRank(graph)
    }

  }

  def iterate(graph: Graph[PartitionID, PartitionID], iteration:Int=0, method:String ): Unit = {

    //         println(graph.vertices.collect().mkString(" "))
    //        println(graph.edges.collect().mkString(" "))
    val ops = new MatrixOps()
    val mat: RowMatrix = ops.createMatrix(sc, graph, laplacian = false)

    val eigenCount: Int = if(graph.numVertices> 100) 30 else mat.numCols().toInt
    val svd = mat.computeSVD(eigenCount, computeU = true)
    val U: RowMatrix = svd.U
    val s: Vector = svd.s
    val V: Matrix = svd.V
    println(method+"\t"+iteration+"\t" +graph.numVertices+"\t"+graph.numEdges+"\t"+ s.toArray.mkString("\t"))


    val order = if(method=="closeness" || method =="depth") 1 else -1
    val influence: Map[VertexId, Double] = prominenceValues(graph,method)
    val k = if(graph.numVertices>100) (graph.numVertices/100).toInt  else 1

    //choose k candidates to remove
    val sortedValues:ListMap[VertexId, Double] = ListMap(influence.toSeq.sortBy(order* _._2): _*)
    //println(sortedValues.mkString(","))
    val li = new scala.collection.mutable.ArrayBuffer[Long]
    val it = sortedValues.iterator
    for (i <- 1 to k) {
      if(it.hasNext) {
        val n1 = it.next()._1
        li.append(n1)
      }
    }
    //       println("Removing "+li.mkString(" and "))
    val newEdges = graph.edges.filter(f => !li.contains(f.srcId) && !li.contains(f.dstId))

    val g2 = Graph.fromEdges(newEdges, defaultValue = 0)

    if(g2.numEdges>0) {
      iterate(g2,iteration+1,method)
    }
  }

}


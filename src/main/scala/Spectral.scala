import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.sql.SparkSession

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
/**
  * Created by cxa123230 on 3/29/2017.
  * https://gist.github.com/vrilleup/9e0613175fab101ac7cd
  */
object Spectral {

  val spark = SparkSession
    .builder
    .appName("GraphDepth")
    .master("local[16]")
    .getOrCreate()
  Logger.getRootLogger().setLevel(Level.ERROR)
  val sc = spark.sparkContext
  def main(args: Array[String]): Unit = {


    val networkName = "karate"

    val g: Graph[Int, Int] = DataLoader.load(sc,networkName)
    var graph: Graph[Int, Int] = GraphCleaning.makeUndirected((g))
    println(s"The $networkName network.")
    var iteration =1
   ff(graph,iteration)
  }



  def ff(graph: Graph[PartitionID, PartitionID],iteration:Int=0): Unit = {
    while(graph.numEdges!=0) {
      println("iteration "+iteration)
      println(graph.numVertices+" vertices, "+graph.numEdges+" edges.")
      //     println(graph.vertices.collect().mkString(" "))
      //     println(graph.edges.collect().mkString(" "))
      val ops = new MatrixOps()
      val mat: RowMatrix = ops.createMatrix(sc, graph, laplacian = true)

      val eigenCount: Int = mat.numCols().toInt
      val svd = mat.computeSVD(eigenCount, computeU = true)
      val U: RowMatrix = svd.U
      val s: Vector = svd.s
      val V: Matrix = svd.V
      println("Eigens: " + s)
      val d = new Centrality()
      val cloValues: Map[VertexId, Int] = d.closeness(graph, depth = false)
      //      println(cloValues.mkString(", "))
      val k = 1

      //choose k candidates to remove
      val sortedValues:ListMap[VertexId, Int] = ListMap(cloValues.toSeq.sortBy(_._2): _*)
      println(sortedValues.mkString(","))
      val li = new scala.collection.mutable.ArrayBuffer[Long]
      val it = sortedValues.iterator
      for (i <- 1 to k) {
        val n1 = it.next()._1
        li.append(n1)

      }
      println("Removing "+li.mkString(" and "))
      val newEdges = graph.edges.filter(f => !li.contains(f.srcId) && !li.contains(f.dstId))

      Graph.fromEdges(newEdges, defaultValue = 0)


    }
  }
}


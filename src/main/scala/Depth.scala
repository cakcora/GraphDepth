import java.io.FileWriter

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{EdgeDirection, Graph, _}
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.graphx.lib.ShortestPaths.SPMap
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by cxa123230 on 11/15/2016.
  */
object Depth {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("GraphDepth")
      .master("local[16]")
      .getOrCreate()
    Logger.getRootLogger().setLevel(Level.ERROR)
    val sc = spark.sparkContext
    val networkName = "karate"

    val sigma = 0.5
    val mu = 3.0
    val g: Graph[PartitionID, PartitionID] = DataLoader.load(sc,networkName)
    val graph: Graph[PartitionID, PartitionID] = GraphCleaning.undirected(g)

    val result:Graph[SPMap, PartitionID] = ShortestPaths.run(graph, graph.vertices.collect.map(e=>e._1))

    val depthValues:Array[(VertexId, PartitionID)] = result
      .vertices
      .map(e=>{
      (e._1,e._2.values.max)
    }
    ).collect

    val closenessValues:Array[(VertexId, PartitionID)] = result
      .vertices
      .map(e=>{
        (e._1,e._2.values.sum)
      }
      ).collect
    val maxCloseness:Double = closenessValues.maxBy(_._2)._2
    println("max closeness: "+maxCloseness)


    val freq:Map[PartitionID, Array[(VertexId, PartitionID)]] = depthValues.groupBy(_._2)
    val ranks:Array[(VertexId, Double)] = graph.pageRank(0.0001).vertices.collect
    for(i <- 0 to depthValues.length-1){
      println((i+1)+"\t"+(depthValues(i))+"\t"+closenessValues(i)+"\t"+ranks(i))
  }
    val combs = depthValues.filter(e=>e._2==3).map(e=>e._1).toSet[VertexId].subsets.map(_.toList).toList
    val betNodes:List[VertexId] = List(1,34,33,3,32,9,2,14)
    val depNodes:List[VertexId] = List(1,3,32,9,2,14,20,4)
    val cloNodes:List[VertexId] = List(1,3,34,32,9,14,33,20)
    val pagNodes:List[VertexId] = List(34,1,33,3,2,32,4,24)

    depthGain(graph, betNodes)
    depthGain(graph, depNodes)
    depthGain(graph, cloNodes)
    depthGain(graph, pagNodes)
    depthGain(graph, List(-1L))
  }


  def depthGain(graph: Graph[PartitionID, PartitionID], combs: List[VertexId]): Unit = {
    var gain  =0
    var inducedGraph = graph
    for (subset <- combs) {

      val nGr: RDD[(VertexId, VertexId)] = inducedGraph.edges.filter(f => subset!=(f.dstId) && subset!=(f.srcId)).map(e => (e.srcId, e.dstId))
      val nGraph = Graph.fromEdgeTuples(nGr, defaultValue = 0)
      val result: Graph[SPMap, PartitionID] = ShortestPaths.run(nGraph, nGraph.vertices.collect.map(e => e._1))
      val t = totalDepth(result)
      gain+= t
      //inducedGraph=nGraph
      println("\t"+subset+" "+t)
    }
    println(gain + ":total depth")
  }

  def totalDepth(result: Graph[SPMap, PartitionID]): Int = {
    result
      .vertices
      .map(e => {
        (e._2.values.max)
      }
      ).collect.sum

  }
}

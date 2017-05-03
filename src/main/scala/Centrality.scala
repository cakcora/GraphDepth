
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.graphx.lib.ShortestPaths._

import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph
import scala.collection.mutable

/**
  * Created by cxa123230 on 4/5/2017.
  */
class Centrality {

  def pageRank(graph:Graph[Int,Int]):Map[VertexId, Double]={
   graph.pageRank(0.0001).vertices.collect().toMap
  }

  def degree(graph: Graph[Int, Int]):Map[VertexId, Double] ={
    val degrees = graph.collectNeighborIds(EdgeDirection.Either).map(e=>(e._1 -> e._2.length.toDouble)).collect.toMap
    return degrees
  }

  def closeness(graph: Graph[Int, Int], depth:Boolean): Map[VertexId, Double] ={
    val vertices: Array[VertexId] = graph.vertices.map(e => e._1).collect()
    val result:Graph[SPMap, Int] = ShortestPaths.run(graph, vertices)

    val depthValues:Map[VertexId, Double] = result
      .vertices
      .map(e=>{
        (e._1,e._2.values.max.toDouble)
      }
      ).collect.toMap

    if(depth) return depthValues

    val closenessValues:Map[VertexId, Double] = result
      .vertices
      .map(e=>{
        (e._1,e._2.values.sum.toDouble)
      }
      ).collect.toMap
    //val maxCloseness:Double = closenessValues.maxBy(_._2)._2
    //    println("max closeness: "+maxCloseness)

    return closenessValues
  }

}

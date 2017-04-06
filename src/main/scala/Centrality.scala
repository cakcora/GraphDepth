import org.apache.spark.graphx.{Graph, _}
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.graphx.lib.ShortestPaths._

import scala.collection.mutable

/**
  * Created by cxa123230 on 4/5/2017.
  */
class Centrality {

  def closeness(graph: Graph[Int, Int], depth:Boolean=false): Map[VertexId, Int] ={
    val vertices: Array[VertexId] = graph.vertices.map(e => e._1).collect()
    val result:Graph[SPMap, Int] = ShortestPaths.run(graph, vertices)

    val depthValues:Map[VertexId, Int] = result
      .vertices
      .map(e=>{
        (e._1,e._2.values.max)
      }
      ).collect.toMap

    if(depth) return depthValues

    val closenessValues:Map[VertexId, Int] = result
      .vertices
      .map(e=>{
        (e._1,e._2.values.sum)
      }
      ).collect.toMap
//    val maxCloseness:Double = closenessValues.maxBy(_._2)._2
//    println("max closeness: "+maxCloseness)

    return closenessValues
  }

}

import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeDirection, Graph, _}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by cxa123230 on 4/6/2017.
  */
class MatrixOps {
  def createMatrix(sc: SparkContext, graph: Graph[Int, Int], laplacian:Boolean=false): RowMatrix = {
    val ne: Map[VertexId, Set[VertexId]] = graph.collectNeighborIds(EdgeDirection.Either).map(e => (e._1 -> e._2.toSet)).collect().toMap

    val nodeCount = graph.numVertices.toInt

    val rm: ListBuffer[Vector] = new ListBuffer[Vector]()
    val id = new mutable.HashMap[Int,Int]()
    var k =0

    val vertices: Array[PartitionID] = graph.vertices.map(e => e._1.toInt).collect()
    for(v<-vertices){
      id(v)=k
      k = k+1
    }
    val sign = if(laplacian) -1 else 1
    for (x <- vertices) {

      val valArr = new ArrayBuffer[Double]
      val indArr = new ArrayBuffer[PartitionID]
      indArr.appendAll(ne(x).map(e => id(e.toInt)))
      valArr.appendAll(List.fill(ne(x).size)(sign*1.0))

      if (laplacian) {
        indArr.append(id(x))
        valArr.append(1.0 * ne(x).size)
      }
      rm.append(Vectors.sparse(nodeCount, indArr.toArray, valArr.toArray))
    }
    val mat = new RowMatrix(sc.makeRDD(rm))


    mat
  }
}

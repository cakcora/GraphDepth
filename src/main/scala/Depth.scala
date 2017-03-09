import java.io.FileWriter

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{EdgeDirection, Graph, _}
import org.apache.spark.graphx.lib.ShortestPaths
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
    val networkName = "lognormal"

    val sigma = 0.5
    val mu = 3.0
    val grOptions: Map[String, AnyVal] = Map(("mu", mu), ("sigma", sigma), ("vertices", 10))
    val graph: Graph[Int, Int] = DataLoader.load(sc, networkName, grOptions)
    val degreeMap: Map[Int, Int] = graph.collectNeighborIds(EdgeDirection.Either).collect().map(e => (e._1.toInt, e._2.length)).toMap
    val result = ShortestPaths.run(graph, Seq(1))


  }


}

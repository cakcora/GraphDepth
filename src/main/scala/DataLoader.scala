import breeze.linalg.*
import org.apache.spark.SparkContext
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.graphx.{Graph, GraphLoader, PartitionStrategy}
import org.apache.spark.rdd.RDD

/**
  * Created by cxa123230 on 11/3/2016.
  */
object DataLoader {

  def getTestGraph(sc: SparkContext): Graph[Int, Int] = {
    val re: RDD[(Long, Long)] = sc.parallelize(Array((1L, 2L), (3L, 1L), (2L, 4L), (5L, 2L), (5L, 4L), (5L, 6L),(7L, 4L)))
    val tupleGraph = Graph.fromEdgeTuples(re, defaultValue = 0,
      uniqueEdges = Some(PartitionStrategy.RandomVertexCut))
    tupleGraph
  }

  def load(sc: SparkContext, graphType: String, options: Map[String, AnyVal] = Map()): Graph[Int, Int] = {

    val dir: String = "C://Projects/DisagioData/"
    graphType match {

      case "dblp" => {
        GraphLoader.edgeListFile(sc, dir + "dblpgraph.txt")
      }
      case "facebook" => {
        GraphLoader.edgeListFile(sc, dir+"facebook-links.txt")
      }
      case "enron" => {
        GraphLoader.edgeListFile(sc, dir+"Email-Enron.txt")
      }
      case "wiki-talk" => {
        GraphLoader.edgeListFile(sc, dir+"WikiTalk.txt")
      }
      case "wiki-vote" => {
        GraphLoader.edgeListFile(sc, dir+"Wiki-Vote.txt")
      }
      case "gowalla" => {
        GraphLoader.edgeListFile(sc, dir+"Gowalla_edges.txt")
      }
      case "kite" => {
        GraphLoader.edgeListFile(sc, dir+"Brightkite_edges.txt")
      }
      case "epinions" => {
        GraphLoader.edgeListFile(sc, dir+"soc-Epinions1.txt")
      }

      //-------------------------------------------
      case "patent" =>{
        GraphLoader.edgeListFile(sc, dir+"cit-Patents.txt")
      }
      case "livejournal" =>{
        GraphLoader.edgeListFile(sc, dir+"com-lj.ungraph.txt")
      }
      case "as-skitter" =>{
        GraphLoader.edgeListFile(sc, dir+"as-skitter.txt")
      }
      case "orkut" =>{
        GraphLoader.edgeListFile(sc, dir+"com-orkut.ungraph.txt")
      }
      case "youtube" =>{
        GraphLoader.edgeListFile(sc, dir+"com-youtube.ungraph.txt")
      }
      case "amazon" =>{
        GraphLoader.edgeListFile(sc, dir+"com-amazon.ungraph.txt")
      }
      //------------------------------
      case "web-google" =>{
        GraphLoader.edgeListFile(sc, dir+"web-Google.txt")
      }
      case "web-notredame" =>{
        GraphLoader.edgeListFile(sc, dir+"web-NotreDame.txt")
      }
      case "web-berkeley" => {
        GraphLoader.edgeListFile(sc, dir+"web-BerkStan.txt")
      }
      case "web-stanford" => {
        GraphLoader.edgeListFile(sc, dir+"web-Stanford.txt")
      }
      //-----------------------------------------------------------
      case "Ca-CondMat" => {
        GraphLoader.edgeListFile(sc, dir+"CA-CondMat.txt")
      }
      case "CA-AstroPh" => {
        GraphLoader.edgeListFile(sc, dir+"CA-AstroPh.txt")
      }
      case "CA-GrQc" => {
        GraphLoader.edgeListFile(sc, dir+"CA-GrQc.txt")
      }
      case "CA-HepPh" => {
        GraphLoader.edgeListFile(sc, dir+"CA-HepPh.txt")
      }
      case "CA-HepTh" => {
        GraphLoader.edgeListFile(sc, dir+"CA-HepTh.txt")
      }

      //------------------------------------------
      case "karate" => {
        GraphLoader.edgeListFile(sc, dir+"karate.txt")
      }
      case "topologyC3" => {
        GraphLoader.edgeListFile(sc, dir+"topologyC3.txt")
      }
      case "grid" => {
        val dim = Math.pow(options("vertices").asInstanceOf[Int], 0.5).toInt
        val g: Graph[(Int, Int), Double] = GraphGenerators.gridGraph(sc, dim, dim)
        val gra: Graph[Int, Int] = g.mapVertices((a, b) => 1).mapEdges(a => 1)
        GraphCleaning.cleanGraph(sc, gra)
      }
      case "lognormal" => {
        val mu = options("mu").asInstanceOf[Double]
        val sigma = options("sigma").asInstanceOf[Double]
        val vertices = options("vertices").asInstanceOf[Int]
        val gr: Graph[Long, Int] = GraphGenerators.logNormalGraph(sc, vertices, 1, mu, sigma).removeSelfEdges()
        GraphCleaning.cleanGraph(sc, gr.mapVertices((a, b) => a.toInt))

      }
      case "rmat" =>{
        val requestedNumVertices: Int = options("vertices").asInstanceOf[Int]
        val edgeDensity: Int = options("edgeDensity").asInstanceOf[Int]
        GraphGenerators.rmatGraph(sc, requestedNumVertices, edgeDensity * requestedNumVertices)
      }
      case "ring" => {
        val v = options("vertices").asInstanceOf[Int]
        val sd = sc.makeRDD( for (x <- 1 to v)
          yield ((x.toLong),if (x+1==v) v.toLong else ((x+1)%v).toLong)
        )
        Graph.fromEdgeTuples(sd,defaultValue = 1)
      }
      case "complete" =>{
        val v: Int = options("vertices").asInstanceOf[Int]
        val sd = sc.makeRDD(
          for (x <- 1 to  v; y <- x+1 to  options("vertices").asInstanceOf[Int]) yield (x.toLong,y.toLong)
        )
        Graph.fromEdgeTuples(sd,defaultValue = 1)
      }
      case _: String => {
        println("No preference for graph type: Using a random star graph.")
        GraphGenerators.starGraph(sc, 100)
      }
    }
  }
}

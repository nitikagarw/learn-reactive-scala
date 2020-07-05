package lectures

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{
  Balance,
  Broadcast,
  Flow,
  GraphDSL,
  Merge,
  RunnableGraph,
  Sink,
  Source,
  Zip
}
import akka.stream.{ActorMaterializer, ClosedShape}

object GraphBasics extends App {
  implicit val system: ActorSystem = ActorSystem("GraphBasics")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1) //hard computation
  val multiplier = Flow[Int].map(x => x * 10) //hard computation
  val output = Sink.foreach[(Int, Int)](println)

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] => // builder = MUTABLE data structure
      import GraphDSL.Implicits._

      //Add the necessary components of the graph
      val broadcast = builder.add(Broadcast[Int](2)) // fan-out operator
      val zip = builder.add(Zip[Int, Int]) // fan-in operator

      //tying up the components
      input ~> broadcast
      broadcast.out(0) ~> incrementer ~> zip.in0
      broadcast.out(1) ~> multiplier ~> zip.in1
      zip.out ~> output

      ClosedShape // FREEZE the builder shape
  })
//  graph.run()

  /**
    * Exercise:
    * - Feed a source into 2 sinks at a same time (use broadcast)
    */
  val firstSink = Sink.foreach[Int](x => println(s"First sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Second sink: $x"))

  val sourceToTwoSinksGraph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._
      //Add the necessary components of the graph
      val broadcast = builder.add(Broadcast[Int](2))

      //tying up the components
      input ~> broadcast ~> firstSink
      broadcast ~> secondSink //implicit port numbering

      ClosedShape
  })

  /**
    * Exercise:
    * - Balance and Merge
    */
  import scala.concurrent.duration._
  val fastSource = input.throttle(5, 1 second)
  val slowSource = input.throttle(2, 1 second)

  val sink1 = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink1 number of elements: $count")
    count + 1
  })
  val sink2 = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink2 number of elements: $count")
    count + 1
  })

  val balanceGraph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      //Add the necessary components of the graph
      val merge = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2))

      //tying up the components
      fastSource ~> merge ~> balance ~> sink1
      slowSource ~> merge; balance ~> sink2

      ClosedShape
  })

  balanceGraph.run()
}

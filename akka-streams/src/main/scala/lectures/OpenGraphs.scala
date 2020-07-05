package lectures

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}

object OpenGraphs extends App {
  implicit val system: ActorSystem = ActorSystem("OpenGraphs")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * A composite source that concatenates two sources
    * - emits all the elements from hte first source
    * - then all the elements from the second source
    */
  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  val sourceGraph = Source.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val concat = builder.add(Concat[Int](2))

    firstSource ~> concat
    secondSource ~> concat
    SourceShape(concat.out)
  })

//  sourceGraph.to(Sink.foreach(println)).run()

  /**
    * Complex Sink
    */
  val sink1 = Sink.foreach[Int](x => println(s"Meaningful 1 : $x"))
  val sink2 = Sink.foreach[Int](x => println(s"Meaningful 2 : $x"))

  val sinkGraph = Sink.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Int](2))

    broadcast ~> sink1
    broadcast ~> sink2

    SinkShape(broadcast.in)
  })
//  firstSource.to(sinkGraph).run()

  /**
    * Challenge: complex Flow
    * Write your own flow that's composed of two other flows:
    * - one that adds 1 to the number
    * - one that does number * 10
    */
  val incrementer = Flow[Int].map(x => x + 1) //hard computation
  val multiplier = Flow[Int].map(x => x * 10) //hard computation

  val flowGraph = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      //everything OPERATES on SHAPES

      // define auxiliary SHAPES
      val incrementerShape = builder.add(incrementer)
      val multiplierShape = builder.add(multiplier)

      // connect the SHAPES
      incrementerShape ~> multiplierShape

      FlowShape(incrementerShape.in, multiplierShape.out) // SHAPE
    } // static graph
  ) //component

  firstSource.via(flowGraph).to(Sink.foreach(println)).run()

  /**
    * Flow from a source and a sink
    */
  def flowFromSinkAndSource[A, B](sink: Sink[A, _],
                                  source: Source[B, _]): Flow[A, B, _] = {
    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      val sourceShape = builder.add(source)
      val sinkShape = builder.add(sink)

      FlowShape(sinkShape.in, sourceShape.out)
    })
  }

  val f = Flow.fromSinkAndSourceCoupled(
    Sink.foreach[String](println),
    Source(1 to 10)
  )
}

package lectures

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

object BackpressureBasics extends App {

  implicit val system: ActorSystem = ActorSystem("BackpressureBasics")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] { x =>
    //simulate a long processing
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

//  fastSource.to(slowSink).run() //fusing? //not backpressure

  // backpressure
//  fastSource.async.to(slowSink).run()

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

  fastSource.async
    .via(simpleFlow)
    .async
    .to(slowSink)
//    .run()

  /**
    * reactions to backpressure (in order):
    * - try to slow down if possible
    * - buffer elements until there's more demand
    * - drop elements from buffer if it overflows
    * - tear down/kill the whole stream (failure)
    */
  val bufferedFlow = simpleFlow.buffer(10, OverflowStrategy.dropHead)
  fastSource.async
    .via(bufferedFlow)
    .async
    .to(slowSink)
    .run()

  /**
    * OverFlow Strategies:
    * - dropHead = oldest
    * - dropTail = new
    * - dropNew = exact element to be added = keeps the buffer
    * - drop the entire buffer
    * - backpressure signal
    * - fail
    */
  //throttling
  import scala.concurrent.duration._
  fastSource.throttle(2, 1 second).runWith(Sink.foreach(println))
}

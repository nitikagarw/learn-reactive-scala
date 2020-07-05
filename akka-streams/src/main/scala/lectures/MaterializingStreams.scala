package lectures

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object MaterializingStreams extends App {
  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher // for execution context

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
//  val simpleMaterializedValue = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b)
//  val sumFuture = source.runWith(sink)
//  sumFuture.onComplete {
//    case Success(value) => println(s"Sum of all elements is $value")
//    case Failure(exception) =>
//      println(s"The sum of the elements couldn't be computed $exception")
//  }

  //choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simplesink = Sink.foreach[Int](println)
  val graph =
    simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simplesink)(Keep.right)
  graph.run().onComplete {
    case Success(_) => println("Stream processing finished")
    case Failure(exception) =>
      println(s"Stream processing failed with $exception")
  }

  //syntactic sugar
  val sum = Source(1 to 10)
    .runWith(Sink.reduce[Int](_ + _)) // source.to(Sink.reduce)(Keep.right)
  Source(1 to 10).runReduce[Int](_ + _) //same as above
  //backwards
  Sink
    .foreach[Int](println)
    .runWith(Source.single(42)) // source(...).to(sink(...)).run()

  //both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simplesink)

  /**
    * - return the last element out of the source (use sink.last)
    * - compute total word count out of the stream of sentences (use map, fold, reduce)
    */
  val f1 = Source(1 to 10).toMat(Sink.last)(Keep.right).run()
  val f2 = Source(1 to 10).runWith(Sink.last)

  val sentenceSource = Source(List("Akka is awesome", "I love streams"))
  val wordCountSink = Sink.fold[Int, String](0)(
    (currWords, newSentence) => currWords + newSentence.split(" ").length
  )

  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)(
    (currWords, newSentence) => currWords + newSentence.split(" ").length
  )

  val wordCountFlow = Flow[String].fold[Int](0)(
    (currWords, newSentence) => currWords + newSentence.split(" ").length
  )

  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource
    .viaMat(wordCountFlow)(Keep.right)
    .toMat(Sink.head)(Keep.right)
    .run()

  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2

}

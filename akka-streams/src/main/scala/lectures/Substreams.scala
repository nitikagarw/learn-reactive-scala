package lectures

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.util.{Failure, Success}

object Substreams extends App {

  implicit val system: ActorSystem = ActorSystem("Substreams")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * 1 - Grouping a stream by a certain function
    */
  val wordsSource = Source(
    List("Akka", "is", "amazing", "learning", "substreams")
  )
  val groups =
    wordsSource.groupBy(
      30,
      word => if (word.isEmpty) "\0" else word.toLowerCase.charAt(0)
    )

  groups
    .to(Sink.fold(0)((count, word) => {
      val nCount = count + 1
      println(s"I just received $word, count is $nCount")
      nCount
    }))
    .run()

  /**
    * 2 - merge substreams back
    */
  val textSource = Source(
    List("I love Akka Streams", "this is amazing", "Learning from rock the jvm")
  )

  val totalCharacterCountFuture = textSource
    .groupBy(2, str => str.length % 2)
    .map(_.length) //Do you expensive computation here
    .mergeSubstreamsWithParallelism(2)
    .toMat(Sink.reduce[Int](_ + _))(Keep.right)
    .run()

  import system.dispatcher
  totalCharacterCountFuture.onComplete {
    case Success(value) => println(s"Total Char Count: $value")
    case Failure(exception) =>
      println(s"Char computation FAILED: $exception")
  }

  /**
    * 3 - Splitting a stream into substreams, when a condition is met
    */
  val text = "I love Akka Streams\n" + "this is amazing\n" + "Learning from rock the jvm\n"

  val anotherCharCountFuture = Source(text.toList)
    .splitWhen(c => c == '\n')
    .filter(_ != '\n')
    .map(_ => 1)
    .mergeSubstreams
    .toMat(Sink.reduce[Int](_ + _))(Keep.right)
    .run()

  anotherCharCountFuture.onComplete {
    case Success(value) => println(s"Total Char Count alternative: $value")
    case Failure(exception) =>
      println(s"Char computation FAILED: $exception")
  }

  /**
    * 4 - Flattening
    */
  val simpleSource = Source(1 to 5)

  simpleSource
    .flatMapConcat(x => Source(x to (3 * x)))
    .runWith(Sink.foreach(println))

  simpleSource
    .flatMapMerge(2, x => Source(x to (3 * x)))
    .runWith(Sink.foreach(println))

}

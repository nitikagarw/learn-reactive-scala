package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  HttpRequest
}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import spray.json._

import scala.util.{Failure, Success}

object RequestLevel extends App with PaymentJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("RequestLevel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import PaymentSystemDomain._
  import system.dispatcher

  val responseFuture =
    Http().singleRequest(HttpRequest(uri = "http://www.google.com"))

  responseFuture.onComplete {
    case Success(response) =>
      response.discardEntityBytes()
      println(s"The request was successful and returned: $response")
    case Failure(exception) =>
      println(s"The request failed: $exception")
  }

  val creditCards = List(
    CreditCard("1919-2937-4729-2695", "6595", "test-account-35595"),
    CreditCard("1234-1234-1234-1234", "9095", "test-account-13806"),
    CreditCard("1386-1759-7503-2529", "2750", "test-account-57405")
  )

  val paymentRequest = creditCards.map { creditCard =>
    PaymentRequest(creditCard, "rtjvm-store-account", 99)
  }

  val serverRequests = paymentRequest.map(
    paymentRequest =>
      HttpRequest(
        HttpMethods.POST,
        uri = "http://localhost:8080/api/payments",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentRequest.toJson.prettyPrint
        )
    )
  )

  Source(serverRequests)
    .mapAsyncUnordered(10)(request => Http().singleRequest(request))
    .runForeach(println)
}

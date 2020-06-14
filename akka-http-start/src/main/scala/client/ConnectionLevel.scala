package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ConnectionLevel extends App with PaymentJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("ConnectionLevel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import PaymentSystemDomain._
  import system.dispatcher

  val connectionFlow = Http().outgoingConnection("www.google.com")

  def oneOffRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  oneOffRequest(HttpRequest()).onComplete {
    case Success(response) =>
      println(s"Got successful response: $response")
    case Failure(exception) =>
      println(s"Sending the request Failed: $exception")
  }

  /**
    * A small Payments System
    */
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
        uri = Uri("/api/payments"),
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentRequest.toJson.prettyPrint
        )
    )
  )

  Source(serverRequests)
    .via(Http().outgoingConnection("localhost", 8080))
    .to(Sink.foreach[HttpResponse](println))
    .run()
}

package client

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import spray.json._

import scala.util.{Failure, Success}

object HostLevel extends App with PaymentJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("HostLevel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import PaymentSystemDomain._

  val poolFlow = Http().cachedHostConnectionPool[Int]("www.google.com")

  Source(1 to 10)
    .map(i => (HttpRequest(), i))
    .via(poolFlow)
    .map {
      case (Success(response), value) =>
        // VERY IMPORTANT ELSE IT WILL BLOCK => LEAKING CONNECTIONS
        response.discardEntityBytes()
        s"Request $value has received response: $response"
      case (Failure(ex), value) =>
        s"Request $value has failed with exception: $ex"
    }
//    .runWith(Sink.foreach[String](println))

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
      (
        HttpRequest(
          HttpMethods.POST,
          uri = Uri("/api/payments"),
          entity = HttpEntity(
            ContentTypes.`application/json`,
            paymentRequest.toJson.prettyPrint
          )
        ),
        UUID.randomUUID().toString
    )
  )

  Source(serverRequests)
    .via(Http().cachedHostConnectionPool[String]("localhost", 8080))
    .runForeach { // (Try[HttpResponse], String)
      case (
          Success(response @ HttpResponse(StatusCodes.Forbidden, _, _, _)),
          orderID
          ) =>
        println(s"the orderID: $orderID was not allowed to proceed: $response")
      case (Success(response), orderID) =>
        println(
          s"The orderID $orderID was successful and returned the response: $response"
        )
      // Do something with order: dispatch, send notification, etc.
      case (Failure(ex), orderID) =>
        println(s"The orderID: $orderID could not be completed..!!")
    }

}

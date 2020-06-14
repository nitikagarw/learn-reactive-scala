package client

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

case class CreditCard(serialNumber: String,
                      securityCode: String,
                      account: String)

object PaymentSystemDomain {
  case class PaymentRequest(creditCard: CreditCard,
                            receiverAccount: String,
                            amount: Double)
  case object PaymentAccepted
  case object PaymentRejected
}

trait PaymentJsonProtocol extends DefaultJsonProtocol {
  implicit val creditCardFormat: RootJsonFormat[CreditCard] = jsonFormat3(
    CreditCard
  )
  implicit val paymentRequestFormat
    : RootJsonFormat[PaymentSystemDomain.PaymentRequest] = jsonFormat3(
    PaymentSystemDomain.PaymentRequest
  )
}

class PaymentValidator extends Actor with ActorLogging {
  import PaymentSystemDomain._
  override def receive: Receive = {
    case PaymentRequest(
        CreditCard(serialNumber, _, senderAccount),
        receiverAccount,
        amount
        ) =>
      log.info(
        s"The sender Account($senderAccount) is trying to send $amount dollars to $receiverAccount"
      )
      if (serialNumber == "1234-1234-1234-1234") sender() ! PaymentRejected
      else sender() ! PaymentAccepted
  }
}

object PaymentSystem
    extends App
    with PaymentJsonProtocol
    with SprayJsonSupport {

  // Microservice for payments
  implicit val system: ActorSystem = ActorSystem("PaymentSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import PaymentSystemDomain._
  import system.dispatcher

  val paymentValidator =
    system.actorOf(Props[PaymentValidator], "paymentValidator")

  implicit val defaultTimeout: Timeout = Timeout(2 seconds)

  val paymentRoute =
    path("api" / "payments") {
      post {
        entity(as[PaymentRequest]) { paymentRequest =>
          val validationResponseFuture: Future[StatusCode] =
            (paymentValidator ? paymentRequest).map {
              case PaymentRejected => StatusCodes.Forbidden
              case PaymentAccepted => StatusCodes.OK
              case _               => StatusCodes.BadRequest
            }
          complete(validationResponseFuture)
        }
      }
    }

  Http().bindAndHandle(paymentRoute, "localhost", 8080)
}

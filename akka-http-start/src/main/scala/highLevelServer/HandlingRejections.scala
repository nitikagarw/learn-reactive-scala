package highLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{
  MethodRejection,
  MissingQueryParamRejection,
  Rejection,
  RejectionHandler
}
import akka.stream.ActorMaterializer

object HandlingRejections extends App {

  implicit val system: ActorSystem = ActorSystem("HandlingRejections")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val simpleroute =
    path("api" / "myEndpoint") {
      get {
        complete(StatusCodes.OK)
      } ~
        parameter('id) { _ =>
          complete(StatusCodes.OK)
        }
    }

  // Rejection handlers
  val badRequestHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I've encountered rejections: $rejections")
    Some(complete(StatusCodes.BadRequest))
  }

  val forbiddenHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I've encountered rejections: $rejections")
    Some(complete(StatusCodes.Forbidden))
  }

  // RejectionHandler.default => Implicitly available at the top level
  val simpleRouteWithhandlers =
    handleRejections(badRequestHandler) { // handle rejections at the TOP level
      // define server logic
      path("api" / "myEndpoint") {
        get {
          complete(StatusCodes.OK)
        } ~
          post {
            handleRejections(forbiddenHandler) { //  handle rejections from WITHIN
              parameter('myParam) { _ =>
                complete(StatusCodes.OK)
              }
            }
          }
      }
    }

//  Http().bindAndHandle(simpleRouteWithhandlers, "localhost", 8080)

  // Sealing a Route
  implicit val customRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case q: MissingQueryParamRejection =>
          println(s"I got a query param rejection: $q")
          complete("Rejected Query Param!")
      }
      .handle {
        case m: MethodRejection =>
          println(s"I got a method rejection: $m")
          complete("Rejected Method!")
      }
      .result()

  Http().bindAndHandle(simpleroute, "localhost", 8080)
}

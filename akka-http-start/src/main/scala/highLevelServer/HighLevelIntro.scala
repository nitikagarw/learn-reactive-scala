package highLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object HighLevelIntro extends App {

  implicit val system: ActorSystem = ActorSystem("HighLevelIntro")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  //Directives
  import akka.http.scaladsl.server.Directives._
  val simpleRoute: Route =
    path("home") { //DIRECTIVE
      complete(StatusCodes.OK) //DIRECTIVE
    }

  val pathGetRoute: Route =
    path("home") {
      get {
        complete(StatusCodes.OK)
      }
    }

  //Chaining directives with ~
  val chainedRoute: Route =
    path("myEndpoint") {
      get {
        complete(StatusCodes.OK)
      } ~ /** ---- VERY IMPORTANT ----- **/
      post {
        complete(StatusCodes.Forbidden)
      }
    } ~
      path("home") {
        complete(
          HttpEntity(ContentTypes.`text/html(UTF-8)`, """
        |<html>
        | <body>
        |   Hello from Akka HTTP!!
        | </body>
        |</html>
        |""".stripMargin)
        )
      } // Routing Tree
  Http().bindAndHandle(chainedRoute, "localhost", 8080)

}

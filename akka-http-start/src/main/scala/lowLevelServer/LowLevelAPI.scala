package lowLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object LowLevelAPI extends App {

  implicit val system: ActorSystem = ActorSystem("LowLevelAPI")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8000)
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accepted incoming connection from: ${connection.remoteAddress}")
  }

  val serverBindingFuture = serverSource.to(connectionSink).run()
  serverBindingFuture.onComplete {
    case Success(binding) =>
      println("Server Binding successful")
      binding.terminate(2 seconds)
    case Failure(exception) => println(s"Server Binding Failed: $exception")
  }

  /*
  Method 1 - Synchronously serve HTTP Responses
   */

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity =
          HttpEntity(ContentTypes.`text/html(UTF-8)`, """
          |<html>
          | <body>
          |  Hello from Akka HTTP!
          | </body>
          |</html>
          |""".stripMargin)
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
          |<html>
          | <body>
          |  OOPS, The Resource can't be found!
          | </body>
          |</html>
          |""".stripMargin
        )
      )
  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] {
    connection =>
      connection.handleWithSyncHandler(requestHandler)
  }

//  Http().bind("localhost", 8080).runWith(httpSyncConnectionHandler)
//  Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  /*
  Method 2 - Serve back HTTP Responses asynchronously
   */

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) =>
      Future(
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            | <body>
            |  Hello from Akka HTTP!
            | </body>
            |</html>
            |""".stripMargin)
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(
        HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
            |<html>
            | <body>
            |  OOPS, The Resource can't be found!
            | </body>
            |</html>
            |""".stripMargin
          )
        )
      )
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] {
    connection =>
      connection.handleWithAsyncHandler(asyncRequestHandler)
  }

//  Http().bind("localhost", 8080).runWith(httpAsyncConnectionHandler)
//  Http().bindAndHandleAsync(asyncRequestHandler, "localhost", 8080)
  /*
  Method 3 - Async via Akka Streams
   */

  val streamBasedRequestHandler: Flow[HttpRequest, HttpResponse, _] =
    Flow[HttpRequest].map {
      case HttpRequest(HttpMethods.GET, _, _, _, _) =>
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            | <body>
            |  Hello from Akka HTTP!
            | </body>
            |</html>
            |""".stripMargin)
        )
      case request: HttpRequest =>
        request.discardEntityBytes()
        HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
            |<html>
            | <body>
            |  OOPS, The Resource can't be found!
            | </body>
            |</html>
            |""".stripMargin
          )
        )
    }

//  Http().bind("localhost", 8080).runForeach { connection =>
//    connection.handleWith(streamBasedRequestHandler)
//  }

  val bindingFuture =
    Http().bindAndHandle(streamBasedRequestHandler, "localhost", 8080)

  //shutdown the server
  bindingFuture
    .flatMap(binding => binding.unbind())
    .onComplete(_ => system.terminate())

}

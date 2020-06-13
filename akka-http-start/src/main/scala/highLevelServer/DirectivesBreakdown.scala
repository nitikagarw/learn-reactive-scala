package highLevelServer

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpRequest,
  StatusCodes
}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object DirectivesBreakdown extends App {
  implicit val system: ActorSystem = ActorSystem("DirectivesBreakdown")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * Type 1: Filtering Directives
    */
  val simpleHttpMethodRoute =
    post {
      complete(StatusCodes.Forbidden)
    }

  val simplePathRoute =
    path("about") {
      complete(
        HttpEntity(ContentTypes.`application/json`, """
          |<html>
          | <body>
          |   Hello from About Page!!
          | </body>
          |</html>
          |""".stripMargin)
      )
    }

  val complexPathRoute =
    path("api" / "myEndpoint") { // /api/myEndpoint
      complete(StatusCodes.OK)
    }

  val dontConfuseRoute =
    path("api/myEndpoint") { // /api%2FmyEndpoint
      complete(StatusCodes.OK)
    }

  val pathEndRoute =
    pathEndOrSingleSlash { // localhost:8080 OR localhost:8080/
      complete(StatusCodes.OK)
    }

  /**
    * Type 2: Extraction Directives
    */
  val pathExtractionRoute =
    path("api" / "item" / IntNumber) { (itemNumber: Int) =>
      println(s"I've received a number in my path: $itemNumber")
      complete(StatusCodes.OK)
    }

  val pathMultiExtractRoute =
    path("api" / "order" / IntNumber / IntNumber) { (id, inventory) =>
      println(s"I've got TWO numbers in my path: $id, $inventory")
      complete(StatusCodes.OK)
    }

  val queryParamExtractionRoute = // /api/item?id=45
    path("api" / "item") {

      /**
        * - By default, query parameters are extracted as String
        * - Single quote: reference equality => Performance benefits
        */
      parameter('id.as[Int]) { (itemId: Int) =>
        println(s"I've extracted the ID as $itemId")
        complete(StatusCodes.OK)
      }
    }

  val extractRequestRoute =
    path("controlEndpoint") {
      extractRequest { (httpRequest: HttpRequest) =>
        extractLog { (log: LoggingAdapter) =>
          log.info(s"I've got the HTTPRequest as $httpRequest")
          complete((StatusCodes.OK))
        }
      }
    }

  Http().bindAndHandle(extractRequestRoute, "localhost", 8082)

  /**
    * Type 3: Composite Directives
    */
  val simpleNestedRoute =
    path("api" / "item") {
      get {
        complete(StatusCodes.OK)
      }
    }

  val compactSimpleNestedRoute = (path("api" / "item") & get) {
    complete(StatusCodes.OK)
  }

  val compactExtractRequestRoute =
    (path("controlEndpoint") & extractRequest & extractLog) { (request, log) =>
      log.info(s"I've got the HTTPRequest as $request")
      complete((StatusCodes.OK))
    }

  // /about AND /aboutUs
  val repeatedRoute =
    path("about") {
      complete(StatusCodes.OK)
    } ~
      path("aboutUs") {
        complete(StatusCodes.OK)
      }

  val dryRoute =
    (path("about") | path("aboutUs")) {
      complete(StatusCodes.OK)
    }

  // yourblog.com/42 AND yourblog.com?postId=42

  val blogByIdRoute =
    path(IntNumber) { (blogId: Int) =>
      //Complex Server Logic
      complete(StatusCodes.OK)
    }

  val blogByQueryParamRoute =
    parameter('postId.as[Int]) { (blogPostId: Int) =>
      //Complex Server Logic
      complete(StatusCodes.OK)
    }

  val combinedBlogByIdRoute = (path(IntNumber) | parameter('postId.as[Int])) {
    (blogPostId: Int) =>
      //Complex Server Logic
      complete(StatusCodes.OK)
  }

  /**
    * Type 4: "actionable" directives
    */
  val completeOKRoute = complete(StatusCodes.OK)

  val failedRoute =
    path("notSupported") {
      failWith(new RuntimeException("Unsupported")) // completes with HTTP 500
    }

  val routeWithRejection =
    path("home") {
      reject
    } ~
      path("index") {
        completeOKRoute
      }

  /**
    * Exercise
    */
  val getOrPutPath =
    path("api" / "myEndpoint") {
      get {
        completeOKRoute
      } ~
        post {
          complete(StatusCodes.Forbidden)
        }
    }
}

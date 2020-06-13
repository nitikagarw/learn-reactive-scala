package highLevelServer

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import lowLevelServer.{Guitar, GuitarDB, GuitarStoreJsonProtocol}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

object HighLevelExample extends App with GuitarStoreJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("HighLevelExample")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import GuitarDB._
  import system.dispatcher

  /**
    * GET on localhost:8080/api/guitar => Return All guitars in the store
    * GET on localhost:8080/api/guitar?id=X => fetches the guitar associated with id X
    * GET on localhost:8080/api/guitar/X => fetches the guitar associated with id X
    * GET on /api/guitar/inventory?inStock=true/false => returns the guitar in stock as JSON
    */
  /*
setup
   */
  val guitarDb = system.actorOf(Props[GuitarDB], "LowLevelGuitarDB")
  val guitarList = List(
    Guitar("Fender", "StratoCaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach(guitar => guitarDb ! CreateGuitar(guitar))

  implicit val defaultTimeout: Timeout = Timeout(2 seconds)
  val guitarServerRoute =
    path("api" / "guitar") {
      // ALWAYS PUT THE MORE SPECIFIC ROUTE FIRST
      parameter('id.as[Int]) { (guitarId: Int) =>
        get {
          val guitarFuture: Future[Option[Guitar]] =
            (guitarDb ? FindGuitar(guitarId)).mapTo[Option[Guitar]]
          val entityFuture = guitarFuture.map { guitarOption =>
            HttpEntity(
              ContentTypes.`application/json`,
              guitarOption.toJson.prettyPrint
            )
          }
          complete(entityFuture)
        }
      } ~
        get {
          val guitarsFuture: Future[List[Guitar]] =
            (guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
          val entityFuture = guitarsFuture.map { guitars =>
            HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          }
          complete(entityFuture)
        }
    } ~
      path("api" / "guitar" / IntNumber) { guitarId =>
        get {
          val guitarFuture: Future[Option[Guitar]] =
            (guitarDb ? FindGuitar(guitarId)).mapTo[Option[Guitar]]
          val entityFuture = guitarFuture.map { guitarOption =>
            HttpEntity(
              ContentTypes.`application/json`,
              guitarOption.toJson.prettyPrint
            )
          }
          complete(entityFuture)
        }
      } ~
      path("api" / "guitar" / "inventory") {
        get {
          parameter('inStock.as[Boolean]) { (inStock: Boolean) =>
            val guitarsFuture: Future[List[Guitar]] =
              (guitarDb ? FindGuitarsInStock(inStock)).mapTo[List[Guitar]]
            val entityFuture = guitarsFuture.map { guitar =>
              HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            }
            complete(entityFuture)
          }
        }
      }

  def toHttpEntity(payload: String) =
    HttpEntity(ContentTypes.`application/json`, payload)

  val simplifiedGuitarServerRoute =
    (pathPrefix("api" / "guitar") & get) {
      path("inventory") {
        parameter('inStock.as[Boolean]) { (inStock: Boolean) =>
          complete(
            (guitarDb ? FindGuitarsInStock(inStock))
              .mapTo[List[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        }
      } ~
        (path(IntNumber) | parameter('id.as[Int])) { guitarId =>
          complete(
            (guitarDb ? FindGuitar(guitarId))
              .mapTo[Option[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        } ~
        pathEndOrSingleSlash {
          complete(
            (guitarDb ? FindAllGuitars)
              .mapTo[List[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        }
    }

  Http().bindAndHandle(simplifiedGuitarServerRoute, "localhost", 8080)

}

package lowLevelServer

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

case class Guitar(make: String, model: String, quantity: Int = 0)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars
  case class AddQuantity(id: Int, quantity: Int)
  case class FindGuitarsInStock(inStock: Boolean)
}
class GuitarDB extends Actor with ActorLogging {
  import GuitarDB._
  var guitars: Map[Int, Guitar] = Map.empty
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars")
      sender() ! guitars.values.toList

    case FindGuitar(id) =>
      log.info(s"Searching Guitar by Id: $id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar) =>
      log.info(s"Adding Guitar: $guitar with Id: $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

    case AddQuantity(id, quantity) =>
      log.info(s"Trying to add $quantity items for guitar $id")
      val guitar: Option[Guitar] = guitars.get(id)
      val newGuitar: Option[Guitar] = guitar.map {
        case Guitar(make, model, q) => Guitar(make, model, q + quantity)
      }

      newGuitar.foreach(guitar => guitars += (id -> guitar))
      sender() ! newGuitar

    case FindGuitarsInStock(insTock) =>
      log.info(
        s"Searching for all guitars ${if (insTock) "in" else "out of"} stock"
      )
      if (insTock)
        sender() ! guitars.values.filter(_.quantity > 0)
      else
        sender() ! guitars.values.filter(_.quantity == 0)

  }
}

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat: RootJsonFormat[Guitar] = jsonFormat3(Guitar)
}

object LowLevelRest extends App with GuitarStoreJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("LowLevelRest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import GuitarDB._
  import system.dispatcher

  /*
  - GET on localhost:8080/api/guitar => Return All guitars in the store
  - GET on localhost:8080/api/guitar?id=X => fetches the guitar associated with id X
  - POST on localhost:8080/api/guitar => Insert guitar into the store
   */
  /*
  Exercise: Enhance the Guitar case class with a quantity field, by default 0
  - GET on /api/guitar/inventory?inStock=true/false => returns the guitar in stock as JSON
  - POST on /api/guitar/inventory?id=X&quantity=Y => add Y guitars to the stock of guitar with id = X
   */

  //JSON marshalling
  val simpleGuitar = Guitar("Fender", "StratoCaster")
  println(simpleGuitar.toJson.prettyPrint)

  //unmarshalling
  val simpleGuitarJsonString =
    """
    |{
    |  "make": "Fender",
    |  "model": "StratoCaster",
    |  "quantity": 5
    |}
    |""".stripMargin

  println(simpleGuitarJsonString.parseJson.convertTo[Guitar])

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

  /*
  Server Code
   */

  implicit val defaultTimeout: Timeout = Timeout(2 seconds)

  def getGuitar(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt) //Option[Int]
    guitarId match {
      case None =>
        Future(HttpResponse(StatusCodes.NotFound)) //  /api/guitar?id=
      case Some(id: Int) =>
        val guitarFuture: Future[Option[Guitar]] =
          (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
        guitarFuture.map {
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(guitar) =>
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            )
        }
    }
  }

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, uri @ Uri.Path("/api/guitar"), _, _, _) =>
      /*
      Query parameter handling code
       */
      val query = uri.query()
      if (query.isEmpty) {
        val guitarsFuture: Future[List[Guitar]] =
          (guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
        guitarsFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
      } else {
        getGuitar(query)
      }

    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitar"), _, entity, _) =>
      //entities are Source[ByteString]
      val strictEntityFuture = entity.toStrict(3 seconds)
      strictEntityFuture.flatMap { strictEntity =>
        val guitarJsonString = strictEntity.data.utf8String
        val guitar = guitarJsonString.parseJson.convertTo[Guitar]

        val guitarCreatdFuture: Future[GuitarCreated] =
          (guitarDb ? CreateGuitar(guitar)).mapTo[GuitarCreated]

        guitarCreatdFuture.map { _ =>
          HttpResponse(StatusCodes.OK)
        }
      }
    case HttpRequest(
        HttpMethods.GET,
        uri @ Uri.Path("/api/guitar/inventory"),
        _,
        _,
        _
        ) =>
      val query = uri.query()
      val inStockOption = query.get("inStock").map(_.toBoolean)
      inStockOption match {
        case Some(inStock) =>
          val guitarsFuture: Future[List[Guitar]] =
            (guitarDb ? FindGuitarsInStock(inStock)).mapTo[List[Guitar]]
          guitarsFuture.map { guitar =>
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            )
          }
        case None => Future(HttpResponse(StatusCodes.BadRequest))
      }

    case HttpRequest(
        HttpMethods.POST,
        uri @ Uri.Path("/api/guitar/inventory"),
        _,
        _,
        _
        ) =>
      val query = uri.query()
      val guitarId: Option[Int] = query.get("id").map(_.toInt)
      val guitarQuantity: Option[Int] = query.get("quantity").map(_.toInt)

      val validGuitarResponseFuture: Option[Future[HttpResponse]] = for {
        id <- guitarId
        quantity <- guitarQuantity
      } yield {
        val newGuitarFuture: Future[Option[Guitar]] =
          (guitarDb ? AddQuantity(id, quantity)).mapTo[Option[Guitar]]
        newGuitarFuture.map(_ => HttpResponse(StatusCodes.OK))
      }
      validGuitarResponseFuture.getOrElse(
        Future(HttpResponse(StatusCodes.BadRequest))
      )

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future { HttpResponse(status = StatusCodes.NotFound) }
  }

  Http().bindAndHandleAsync(requestHandler, "localhost", 8080)

}

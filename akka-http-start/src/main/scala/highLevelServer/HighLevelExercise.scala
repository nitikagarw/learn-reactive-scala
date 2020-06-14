package highLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.duration._
import scala.util.{Failure, Success}
case class Person(pin: Int, name: String)

trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val personJson: RootJsonFormat[Person] = jsonFormat2(Person)
}

object HighLevelExercise extends App with PersonJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("HighLevelExercise")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  /**
    * Exercise:
    * - GET /api/people => Retrives all the peopleyou have registered
    * - GET /api/people/pin => Retrieve the person by pin, return as JSON
    * - GET /api/people?pin=123 => Retrieve the person by pin, return as JSON
    * - POST /api/people => with a JSON payload denoting a person, add that Person to the database
   **/
  var people = List(Person(1, "Alice"), Person(2, "Bob"), Person(3, "Charlie"))

  val personServerRoute =
    pathPrefix("api" / "people") {
      get {
        (path(IntNumber) | parameter('pin.as[Int])) { (pin: Int) =>
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              people.find(_.pin == pin).toJson.prettyPrint
            )
          )
        } ~
          pathEndOrSingleSlash {
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                people.toJson.prettyPrint
              )
            )
          }
      } ~
        (post & pathEndOrSingleSlash & extractRequest & extractLog) {
          (request, log) =>
            val strictEntityFuture = request.entity.toStrict(2 seconds)
            val personFuture = strictEntityFuture.map(
              _.data.utf8String.parseJson.convertTo[Person]
            )
            onComplete(personFuture) {
              case Success(person) =>
                log.info(s"Got person: $person")
                people = people :+ person
                complete(StatusCodes.OK)
              case Failure(exception) =>
                failWith(exception)

            }

//            personFuture.onComplete {
//              case Success(person) =>
//                log.info(s"Got person: $person")
//                people = people :+ person
//              case Failure(exception) =>
//                log.warning(
//                  s"Something failed with fetching the person from the entiry: $exception"
//                )
//            }
//
//            complete(personFuture.map(_ => StatusCodes.OK).recover {
//              case _ => StatusCodes.InternalServerError
//            })

        }
    }

  Http().bindAndHandle(personServerRoute, "localhost", 8080)
}

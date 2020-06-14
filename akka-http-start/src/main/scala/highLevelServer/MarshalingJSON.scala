package highLevelServer

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
// step 1
import spray.json._

import scala.concurrent.duration._

case class Player(nickname: String, characterClass: String, level: Int)

object GameAreaMap {
  case object GetAllPlayers
  case class GetPlayer(nickname: String)
  case class GetPlayerByClass(characterClass: String)
  case class AddPlayer(player: Player)
  case class RemovePlayer(player: Player)
  case object OperationSuccess
}

class GameAreaMap extends Actor with ActorLogging {

  import GameAreaMap._
  var players: Map[String, Player] = Map.empty

  override def receive: Receive = {

    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList

    case GetPlayer(nickname) =>
      log.info(s"Getting player with nickname: $nickname")
      sender() ! players.get(nickname)

    case GetPlayerByClass(characterClass) =>
      log.info(s"Getting all Players with characterClass: $characterClass")
      sender() ! players.values.toList
        .filter(_.characterClass == characterClass)

    case AddPlayer(player) =>
      log.info(s"Trying to add player: $player")
      players = players + (player.nickname -> player)
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Trying to remove player: $player")
      players = players - player.nickname
      sender() ! OperationSuccess
  }
}

trait PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit val playerFormat = jsonFormat3(Player)
}

object MarshalingJSON
    extends App
    with PlayerJsonProtocol
    with SprayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("MarshalingJSON")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import GameAreaMap._
  import system.dispatcher

  val rtjvmGameMap = system.actorOf(Props[GameAreaMap], "rockTheJVMGameAreaMap")

  val playersList =
    List(
      Player("martin_killz_u", "Warrior", 70),
      Player("roland", "Elf", 67),
      Player("daniel_rock", "Wizard", 30)
    )

  playersList.foreach { player =>
    rtjvmGameMap ! AddPlayer(player)
  }

  /**
    * - GET /api/player => Returns all players in the map as JSON
    * - GET /api/player/{nickname} => Returns the player with the given nickname
    * - GET /api/player?nickname=X => Returns the player with the given nickname=X
    * - GET /api/player/class/{charClass} => Returns all players with given character Class
    * - POST /api/player (JSON payload) => Adds the Player to the Map
    * - DEL /api/player (JSON payload) => Removes the player from the map
    */
  implicit val timeout: Timeout = Timeout(2 seconds)

  val rtjvmGameRoute =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { charClass =>
          complete(
            (rtjvmGameMap ? GetPlayerByClass(charClass)).mapTo[List[Player]]
          )
        } ~
          (path(Segment) | parameter('nickname.as[String])) { nickname =>
            complete((rtjvmGameMap ? GetPlayer(nickname)).mapTo[Option[Player]])
          } ~
          pathEndOrSingleSlash {
            complete((rtjvmGameMap ? GetAllPlayers).mapTo[List[Player]])
          }
      } ~
        post {
          entity(as[Player]) { player =>
            complete(
              (rtjvmGameMap ? AddPlayer(player)).map(_ => StatusCodes.OK)
            )
          }
        } ~
        delete {
          entity(as[Player]) { player =>
            complete(
              (rtjvmGameMap ? RemovePlayer(player)).map(_ => StatusCodes.OK)
            )
          }
        }
    }

  Http().bindAndHandle(rtjvmGameRoute, "localhost", 8080)

}

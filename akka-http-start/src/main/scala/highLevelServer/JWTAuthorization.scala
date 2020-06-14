package highLevelServer

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import scala.util.{Failure, Success}

object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat2(
    LoginRequest
  )
}

object JWTAuthorization extends App with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem("JWTAuthorization")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import SecurityDomain._
  import system.dispatcher

  val superSecretPasswordDB = Map("admin" -> "admin", "Daniel" -> "RockTheJVM")

  val algorithm = JwtAlgorithm.HS256
  val secretKey = "rockthejvmsecret"

  def checkPassword(user: String, pass: String): Boolean = {
    superSecretPasswordDB.contains(user) && superSecretPasswordDB(user) == pass
  }

  def createToken(user: String, expirationPeriodInDays: Int): String = {
    val claims = JwtClaim(
      expiration = Some(
        System.currentTimeMillis() / 1000 + TimeUnit.DAYS
          .toSeconds(expirationPeriodInDays)
      ),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("rockthejvm.com"),
      content = ""
    )
    JwtSprayJson.encode(claims, secretKey, algorithm) // JWT String
  }

  def isTokenExpired(token: String): Boolean = {
    JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
      case Success(claims) =>
        claims.expiration.getOrElse(0L) < (System.currentTimeMillis() / 1000)
      case Failure(exception) => true
    }
  }

  def isTokenValid(token: String): Boolean = {
    JwtSprayJson.isValid(token, secretKey, Seq(algorithm))
  }

  val loginRoute =
    post {
      entity(as[LoginRequest]) {
        case LoginRequest(user, pass) if checkPassword(user, pass) =>
          val token = createToken(user, 1)
          respondWithHeader(RawHeader("Access-Token", token)) {
            complete(StatusCodes.OK)
          }
        case _ => complete(StatusCodes.Unauthorized)
      }
    }

  val authenticatedRoute =
    (path("secureEndpoint") & get) {
      optionalHeaderValueByName("Authorization") {
        case Some(token) =>
          if (isTokenValid(token)) {
            if (isTokenExpired(token)) {
              complete(
                HttpResponse(
                  status = StatusCodes.Unauthorized,
                  entity = "Token Expired"
                )
              )
            } else {
              complete("User access authorized endpoint")
            }
          } else {
            complete(
              HttpResponse(
                status = StatusCodes.Unauthorized,
                entity = "Token is Invalid or has been tampered with..!!"
              )
            )
          }
        case _ =>
          complete(
            HttpResponse(
              status = StatusCodes.Unauthorized,
              entity = "No token provided..!!"
            )
          )
      }
    }

  val route: Route = loginRoute ~ authenticatedRoute

  Http().bindAndHandle(route, "localhost", 8080)

}

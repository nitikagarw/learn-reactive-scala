package lowLevelServer

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object HttpsContext {
  //Step 1: key store
  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keyStoreFile: InputStream =
    getClass.getClassLoader.getResourceAsStream("keystore.pkcs12")
  //new FileInputstream(new File("src/main/resources/keystore.pkcs12"))
  val password = "akka-https".toCharArray
  ks.load(keyStoreFile, password)

  //Step 2: Initialize a key manager
  val keyManagerFactory = KeyManagerFactory.getInstance("SunX509") // PKI: Public Key Infrastructure
  keyManagerFactory.init(ks, password)

  //Step 3: Initialize a trust Manager
  val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  trustManagerFactory.init(ks)

  //Step 4: Initialize an SSL Context
  val sslContext
    : SSLContext = SSLContext.getInstance("TLS") //Transport Layer Security
  sslContext.init(
    keyManagerFactory.getKeyManagers,
    trustManagerFactory.getTrustManagers,
    new SecureRandom()
  )

  //Step 5: Return HTTPS connection context
  val httpsConnectionContext: HttpsConnectionContext =
    ConnectionContext.https(sslContext)
}

object LowLevelHttps extends App {
  implicit val system: ActorSystem = ActorSystem("LowLevelHttps")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

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

  val httpsBinding = Http().bindAndHandleSync(
    requestHandler,
    "localhost",
    8080,
    HttpsContext.httpsConnectionContext
  )
}

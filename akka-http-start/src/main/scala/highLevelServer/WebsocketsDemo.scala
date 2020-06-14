package highLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.CompactByteString

import scala.concurrent.duration._
object WebsocketsDemo extends App {
  implicit val system: ActorSystem = ActorSystem("WebsocketsDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Message: TextMessage vs BinaryMessage
  val textMessage = TextMessage(Source.single("Hello via a text message!"))
  val binaryMessage = BinaryMessage(
    Source.single(CompactByteString("Hello via a binary message!"))
  )

  val html =
    """
    |<html>
    |<head>
    |    <script>
    |        var exampleSocket = new WebSocket("ws://localhost:8080/greeter");
    |        console.log("Starting WebSocket..");
    |
    |        exampleSocket.onmessage = function(event) {
    |            var newChild = document.createElement("div");
    |            newChild.innerText = event.data
    |            document.getElementById("1").appendChild(newChild);
    |        };
    |
    |        exampleSocket.onopen = function(event) {
    |            exampleSocket.send("Socket seems to be open...");
    |        };
    |
    |        exampleSocket.send("socket says: hello, server!");
    |    </script>
    |</head>
    |<body>
    |Starting WebSocket..!!
    |<div id="1">
    |</div>
    |</body>
    |</html>
    |""".stripMargin

  def webSocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
    case tm: TextMessage =>
      TextMessage(Source.single("Server sends back: ") ++ tm.textStream)
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      TextMessage(Source.single("Server received a binary message: "))
  }

  val webSocketRoute =
    (pathEndOrSingleSlash & get) {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
    } ~
      path("greeter") {
        handleWebSocketMessages(webSocketFlow)
      }

  Http().bindAndHandle(webSocketRoute, "localhost", 8080)

  case class SocialPost(owner: String, content: String)

  val socialFeed = Source(
    List(
      SocialPost("Martin", "Scala 3 has been announced"),
      SocialPost("Daniel", "A new Rock the JVM course is available"),
      SocialPost("Martin", "I killed JAVA")
    )
  )

  val socialMessages = socialFeed
    .throttle(1, 2 seconds)
    .map(
      socialPost =>
        TextMessage(s"${socialPost.owner} said: ${socialPost.content}")
    )

  val socialFlow: Flow[Message, Message, Any] =
    Flow.fromSinkAndSource(Sink.foreach[Message](println), socialMessages)

}

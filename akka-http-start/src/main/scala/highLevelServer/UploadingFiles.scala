package highLevelServer

import java.io.File

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Multipart}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}

object UploadingFiles extends App {
  implicit val system: ActorSystem = ActorSystem("UploadingFiles")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //import systems.dispatcher

  val filesRoute: Route =
    (pathEndOrSingleSlash & get) {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |  <body>
            |    <form action="http://localhost:8080/upload" method="post" enctype="multipart/form-data">
            |      <input type="file" name="myFile">
            |      <button type="submit">Upload</button>
            |    </form>
            |  </body>
            |</html>
          """.stripMargin
        )
      )
    } ~
      (path("upload") & extractLog) { log =>
        // handle uploading of files
        // multipart/form-data

        entity(as[Multipart.FormData]) { formdata =>
          // handle file payload
          val partsSource: Source[Multipart.FormData.BodyPart, _] =
            formdata.parts
          val filePartsSink: Sink[Multipart.FormData.BodyPart, Future[Done]] =
            Sink.foreach[Multipart.FormData.BodyPart] { bodyPart =>
              if (bodyPart.name == "myFile") {
                // Create a file
                val fileName = "src/main/resources/download/" + bodyPart.filename
                  .getOrElse("tempFile_" + System.currentTimeMillis())
                val file = new File(fileName)

                log.info(s"Writing to file: $fileName")

                val fileContentsSource: Source[ByteString, _] =
                  bodyPart.entity.dataBytes
                val fileContentsSink: Sink[ByteString, _] =
                  FileIO.toPath(file.toPath)

                // writing the data to the file
                fileContentsSource.runWith(fileContentsSink)
              }
            }

          val writeOperationFuture = partsSource.runWith(filePartsSink)
          onComplete(writeOperationFuture) {
            case Success(_) => complete("File Uploaded..!!")
            case Failure(exception) =>
              complete(s"File failed to upload: $exception")
          }

        }
      }

  Http().bindAndHandle(filesRoute, "localhost", 8080)

}

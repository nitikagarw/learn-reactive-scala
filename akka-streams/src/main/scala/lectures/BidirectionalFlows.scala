package lectures

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}

object BidirectionalFlows extends App {
  implicit val system: ActorSystem = ActorSystem("BidirectionalFlows")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * Cryptography
    */
  def encrypt(n: Int)(str: String): String = str.map(ch => (ch + n).toChar)
  def decrypt(n: Int)(str: String): String = str.map(ch => (ch - n).toChar)

  println(encrypt(3)("Akka"))

  /**
    * BI-DIRECTIONAL FLOW
    */
  val bidiCryptoStaticGraph = GraphDSL.create() { implicit builder =>
    val encryptionFlowShape = builder.add(Flow[String].map(encrypt(3)))
    val decryptionFlowShape = builder.add(Flow[String].map(decrypt(3)))

    BidiShape.fromFlows(encryptionFlowShape, decryptionFlowShape)
  }

  val unencryptedStrings =
    List("akka", "is", "awesome", "testing", "bidirectional", "flows")

  val unencryptedSource = Source(unencryptedStrings)
  val encryptedSource = Source(unencryptedStrings.map(encrypt(3)))

  val cryptoBidiGraph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val unencryptedSourceShape = builder.add(unencryptedSource)
      val encryptedSourceShape = builder.add(encryptedSource)
      val bidi = builder.add(bidiCryptoStaticGraph)
      val encryptedSinkShape = builder.add(
        Sink.foreach[String](string => println(s"Encrypted: $string"))
      )
      val decryptedSinkShape = builder.add(
        Sink.foreach[String](string => println(s"Decrypted: $string"))
      )

      unencryptedSourceShape ~> bidi.in1; bidi.out1 ~> encryptedSinkShape
      decryptedSinkShape <~ bidi.out2; bidi.in2 <~ encryptedSourceShape

      ClosedShape
  })

  cryptoBidiGraph.run()

  /*
    - encrypting/decrypting
    - encoding/decoding
    - serializing/deserializing
 */

}

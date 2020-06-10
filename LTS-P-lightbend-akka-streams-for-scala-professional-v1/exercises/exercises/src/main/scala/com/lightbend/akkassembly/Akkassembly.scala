package com.lightbend.akkassembly

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.duration._

object Akkassembly extends App {
  implicit val system = ActorSystem("akkassembly")
  implicit val materializer = ActorMaterializer()
  implicit val loggingAdapter = system.log
  import system.dispatcher

  val buildTime = 1.millis
  val bodyShop = new BodyShop(buildTime)

  val paintColors = Set(Color("FFFFFF"), Color("000000"), Color("FF00FF"))
  val paintShop = new PaintShop(paintColors)

  val wheelShop = new WheelShop

  val shipmentSize = 10
  val engineShop = new EngineShop(shipmentSize)

  val upgradeShop = new UpgradeShop()

  val qualityAssurance = new QualityAssurance

  val factory = new Factory(bodyShop, paintShop, engineShop, wheelShop, qualityAssurance, upgradeShop)

  val startTime = System.currentTimeMillis()

  factory.orderCars(10000).withTimer("Order Cars").andThen {
    case _ =>
      system.terminate()
  }
}

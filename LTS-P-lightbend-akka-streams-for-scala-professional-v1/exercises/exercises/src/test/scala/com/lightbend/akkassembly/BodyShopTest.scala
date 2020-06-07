package com.lightbend.akkassembly

import akka.stream.scaladsl.Sink
import org.scalatest.FreeSpec

import scala.concurrent.duration._

class BodyShopTest extends FreeSpec with AkkaSpec {
  "cars" - {
    "should return cars at the expected rate" in {
      val bodyShop = new BodyShop(buildTime = 100.millis)

      val cars = bodyShop.cars
        .takeWithin(1050.millis)
        .runWith(Sink.seq)
        .futureValue

      assert(cars.size == 10)
    }
  }
}

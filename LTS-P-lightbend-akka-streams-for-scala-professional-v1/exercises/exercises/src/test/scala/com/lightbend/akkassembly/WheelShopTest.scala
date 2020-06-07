package com.lightbend.akkassembly

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.FreeSpec

class WheelShopTest extends FreeSpec with AkkaSpec {

  "wheels" - {
    "should return a series of wheels" in {
      val numberToRequest = 100
      val wheelShop = new WheelShop

      val wheels = wheelShop.wheels
        .runWith(TestSink.probe[Wheel])
        .request(numberToRequest)
        .expectNextN(numberToRequest)

      assert(wheels.size === numberToRequest)
      assert(wheels.toSet === Set(Wheel()))
    }
  }

}

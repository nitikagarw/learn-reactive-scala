package com.lightbend.akkassembly

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.FreeSpec

class EngineShopTest extends FreeSpec with AkkaSpec {
  "shipments" - {
    "should emit a series of unique shipments" in {
      val shipmentSize = 10
      val numberToRequest = 5

      val engineShop = new EngineShop(shipmentSize)

      val shipments = engineShop.shipments
        .runWith(TestSink.probe[Shipment])
        .request(numberToRequest)
        .expectNextN(numberToRequest)
        .foreach { shipment =>
          assert(shipment.engines.toSet.size === shipmentSize)
        }
    }
    "should emit unique engines from one shipment to the next" in {
      val shipmentSize = 1
      val numberToRequest = 5

      val engineShop = new EngineShop(shipmentSize)

      val engines = engineShop.shipments
        .mapConcat(_.engines)
        .runWith(TestSink.probe[Engine])
        .request(numberToRequest)
        .expectNextN(numberToRequest)

      assert(engines.toSet.size === numberToRequest)
    }
  }
}

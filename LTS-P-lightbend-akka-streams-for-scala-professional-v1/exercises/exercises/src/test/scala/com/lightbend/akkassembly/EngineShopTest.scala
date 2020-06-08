package com.lightbend.akkassembly

import akka.stream.scaladsl.Source
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

  "engines" - {
    "should flatten the shipments into a series of unique engines" in {
      val shipmentSize = 10

      val engineShop = new EngineShop(shipmentSize)

      val engines = engineShop.engines
        .runWith(TestSink.probe[Engine])
        .request(20)
        .expectNextN(20)

      assert(engines.size === 20)
      assert(engines.toSet.size === 20)
    }
  }

  "installEngine" - {
    "should terminate if there are no cars" in {
      val cars = Source.empty[UnfinishedCar]

      val engineShop = new EngineShop(shipmentSize = 10)

      cars.via(engineShop.installEngine)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectComplete()
    }
    "should install an engine in the car." in {
      val car = UnfinishedCar()
      val cars = Source.repeat(car)

      val engineShop = new EngineShop(shipmentSize = 10)

      val carsWithEngines = cars.via(engineShop.installEngine)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectNextN(10)

      carsWithEngines.foreach { carWithEngine =>
        assert(carWithEngine.engine.isDefined)
      }
    }
  }
}

package com.lightbend.akkassembly

import akka.stream.scaladsl.Source
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

  "installWheels" - {
    "should install four wheels on each car" in {
      val wheelShop = new WheelShop

      val cars = Source.repeat(UnfinishedCar())

      val carsWithWheels = cars.via(wheelShop.installWheels)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectNextN(10)

      carsWithWheels.foreach { car =>
        assert(car.wheels.size === 4)
      }
    }
  }

}

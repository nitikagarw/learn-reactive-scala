package com.lightbend.akkassembly

import org.scalatest.FreeSpec

import scala.concurrent.duration._

class FactoryTest extends FreeSpec with AkkaSpec {
  "orderCars" - {
    "should return the requested quantity of cars" in {
      val color = Color("000000")
      val bodyShop = new BodyShop(buildTime = 1.milli)
      val paintShop = new PaintShop(Set(color))
      val engineShop = new EngineShop(shipmentSize = 20)
      val wheelShop = new WheelShop()
      val qualityAssurance = new QualityAssurance()
      val upgradeShop = new UpgradeShop
      val factory = new Factory(bodyShop, paintShop, engineShop, wheelShop, qualityAssurance, upgradeShop)

      val cars = factory.orderCars(12).futureValue

      assert(cars.size === 12)
      cars.foreach { car =>
        assert(car.color === color)
        assert(car.wheels.size === 4)
      }

      val engines = cars.map(_.engine)
      assert(engines.toSet.size === 12)

      val upgrades = cars.map(_.upgrade)
      assert(upgrades.count(_.isEmpty) === 4)
      assert(upgrades.count(_.contains(Upgrade.DX)) === 4)
      assert(upgrades.count(_.contains(Upgrade.Sport)) === 4)
    }
  }
}

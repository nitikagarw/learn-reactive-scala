package com.lightbend.akkassembly

import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.{FreeSpec, FunSuite}

class UpgradeShopTest extends FreeSpec with AkkaSpec {

  "upgrade" - {
    "should upgrade the correct ratio of cars" in {
      val numCars = 12
      val upgradeShop = new UpgradeShop

      val cars = Source(1 to numCars)
        .map(_ => UnfinishedCar())
        .via(upgradeShop.installUpgrades)
        .runWith(Sink.seq)
        .futureValue

      val upgrades = cars.map(_.upgrade)

      assert(upgrades.count(_.isEmpty) === numCars/3)
      assert(upgrades.count(_.contains(Upgrade.DX)) === numCars/3)
      assert(upgrades.count(_.contains(Upgrade.Sport)) === numCars/3)
    }
  }
}

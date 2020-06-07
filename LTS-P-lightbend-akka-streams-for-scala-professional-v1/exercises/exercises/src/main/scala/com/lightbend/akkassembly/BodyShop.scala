package com.lightbend.akkassembly

import akka.stream.scaladsl.Source

import scala.concurrent.duration.FiniteDuration

class BodyShop(buildTime: FiniteDuration) {
  val cars = Source.tick(buildTime, buildTime, UnfinishedCar())
}

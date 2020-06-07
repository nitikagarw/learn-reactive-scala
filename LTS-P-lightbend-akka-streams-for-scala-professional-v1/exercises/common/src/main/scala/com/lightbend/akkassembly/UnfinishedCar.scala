
package com.lightbend.akkassembly

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random


object UnfinishedCar {
  private val random = new Random(4)
  private val busyTimeScale = 10
  private def calculateRandomMillis = random.nextInt(busyTimeScale).millis

  private val paintTime = calculateRandomMillis
  private val installEngineTime = calculateRandomMillis
  private val installWheelsTime = calculateRandomMillis
  private val installUpgradeTime = calculateRandomMillis

}

case class UnfinishedCar(color: Option[Color] = None,
                         engine: Option[Engine] = None,
                         wheels: Seq[Wheel] = Seq.empty,
                         upgrade: Option[Upgrade] = None) {
  import UnfinishedCar._

  def paint(color: Color): UnfinishedCar = {
    busy(0.millis)
    copy(color = Some(color))
  }
  def installEngine(engine: Engine): UnfinishedCar = {
    busy(0.millis)
    copy(engine = Some(engine))
  }
  def installWheels(wheels: Seq[Wheel]): UnfinishedCar = {
    busy(0.millis)
    copy(wheels = wheels)
  }
  def installUpgrade(upgrade: Upgrade): UnfinishedCar = {
    busy(0.millis)
    copy(upgrade = Some(upgrade))
  }
}
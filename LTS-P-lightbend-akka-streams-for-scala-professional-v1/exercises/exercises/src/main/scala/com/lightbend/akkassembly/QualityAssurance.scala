package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import com.lightbend.akkassembly.QualityAssurance.CarFailedInspection

object QualityAssurance {
  case class CarFailedInspection(car: UnfinishedCar) extends IllegalStateException(s"Car failed inspection: $car")
}

class QualityAssurance {
  val inspect: Flow[UnfinishedCar, Car, NotUsed] = {
    val decider: Supervision.Decider = {
      case CarFailedInspection(car) => Supervision.Resume
    }

    Flow[UnfinishedCar].map {
      case UnfinishedCar(Some(color), Some(engine), wheels, upgrade) if wheels.size == 4 =>
        Car(SerialNumber(), color, engine, wheels, upgrade)
      case car => throw CarFailedInspection(car)
    }.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}

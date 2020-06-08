package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.Flow

class QualityAssurance {
  val inspect: Flow[UnfinishedCar, Car, NotUsed] = {
    Flow[UnfinishedCar].collect {
      case UnfinishedCar(Some(color), Some(engine), wheels, upgrade) if wheels.size == 4 =>
        Car(SerialNumber(), color, engine, wheels, upgrade)
    }
  }
}

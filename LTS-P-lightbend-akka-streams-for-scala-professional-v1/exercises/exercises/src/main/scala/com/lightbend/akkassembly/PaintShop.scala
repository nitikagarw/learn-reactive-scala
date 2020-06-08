package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

class PaintShop(colorSet: Set[Color]) {
  val colors: Source[Color, NotUsed] = Source.cycle(() => colorSet.toIterator)

  val paint: Flow[UnfinishedCar, UnfinishedCar, NotUsed] = {
    Flow[UnfinishedCar].zip(colors).map {
      case (car, color) => car.paint(color)
    }
  }
}

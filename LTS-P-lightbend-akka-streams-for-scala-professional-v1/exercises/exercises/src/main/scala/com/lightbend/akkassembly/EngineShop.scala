package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.collection.immutable.Seq

class EngineShop(shipmentSize: Int) {
  val shipments: Source[Shipment, NotUsed] = {
    Source.fromIterator (() => Iterator.continually {
      Shipment(
        Seq.fill(shipmentSize)(Engine())
      )
    })
  }
}

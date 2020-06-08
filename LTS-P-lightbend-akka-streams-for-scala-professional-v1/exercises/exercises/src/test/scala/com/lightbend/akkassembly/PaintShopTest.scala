package com.lightbend.akkassembly

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.FreeSpec

import scala.collection.immutable.Seq
import scala.concurrent.Await

class PaintShopTest extends FreeSpec with AkkaSpec {

  "colors" - {
    "should repeat each color in the color set" in {
      val colorSet = Set(
        Color("FFFFFF"),
        Color("000000"),
        Color("FF00FF")
      )

      val paintShop = new PaintShop(colorSet)

      val colors = paintShop.colors
        .runWith(TestSink.probe[Color])
        .request(colorSet.size * 2)
        .expectNextN(colorSet.size * 2)

      assert(colors === colorSet.toSeq ++ colorSet.toSeq)
    }
  }

  "paint" - {
    "should throw an error if there are no colors" in {
      val paintShop = new PaintShop(Set.empty)
      val cars = Source.repeat(UnfinishedCar())

      cars.via(paintShop.paint)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectError()
    }
    "should terminate if there are no cars" in {
      val paintShop = new PaintShop(Set(Color("000000")))
      val cars = Source.empty[UnfinishedCar]

      cars.via(paintShop.paint)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectComplete()
    }
    "should apply the paint colors to the cars" in {
      val color = Color("000000")
      val car = UnfinishedCar()
      val paintShop = new PaintShop(Set(color))
      val cars = Source.repeat(car)

      cars.via(paintShop.paint)
        .runWith(TestSink.probe[UnfinishedCar])
        .request(10)
        .expectNextN(Seq.fill(10)(car.copy(color = Some(color))))
    }
  }

}

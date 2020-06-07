package com.lightbend.akkassembly

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.FreeSpec

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

}

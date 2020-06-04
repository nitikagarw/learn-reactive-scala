/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

class CoffeeSpec extends BaseSpec {

  import Coffee._

  "coffees" should {
    "contain Akkaccino, MochaPlay and CaffeScala" in {
      beverages should ===(Set[Coffee](Akkaccino, MochaPlay, CaffeScala))
    }
  }

  "Calling apply" should {
    "create the correct Beverage for a known code" in {
      apply("A") should ===(Akkaccino)
      apply("a") should ===(Akkaccino)
      apply("M") should ===(MochaPlay)
      apply("m") should ===(MochaPlay)
      apply("C") should ===(CaffeScala)
      apply("c") should ===(CaffeScala)
    }
    "throw an IllegalArgumentException for an unknown code" in {
      an[IllegalArgumentException] should be thrownBy apply("1")
    }
  }

  "Calling anyOther" should {
    "return an other Coffee than the given one" in {
      forAll(beverages) { coffee => anyOther(coffee) should !==(coffee) }
    }
  }
}

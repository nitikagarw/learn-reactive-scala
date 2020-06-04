/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import scala.util.Random

sealed trait Coffee

object Coffee {

  case object Akkaccino extends Coffee
  case object MochaPlay extends Coffee
  case object CaffeScala extends Coffee

  val beverages: Set[Coffee] =
    Set(Akkaccino, MochaPlay, CaffeScala)

  def apply(code: String): Coffee =
    code.toLowerCase match {
      case "a" => Akkaccino
      case "m" => MochaPlay
      case "c" => CaffeScala
      case _   => throw new IllegalArgumentException(s"""Unknown beverage code "$code"!""")
    }

  def anyOther(beverage: Coffee): Coffee = {
    val others = beverages - beverage
    others.toVector(Random.nextInt(others.size))
  }
}

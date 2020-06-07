package com.lightbend.akkassembly

sealed trait Upgrade

object Upgrade {
  case object DX extends Upgrade
  case object Sport extends Upgrade
}

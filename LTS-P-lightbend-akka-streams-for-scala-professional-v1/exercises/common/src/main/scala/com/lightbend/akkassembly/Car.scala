package com.lightbend.akkassembly

case class Car(serialNumber: SerialNumber, color: Color, engine: Engine, wheels: Seq[Wheel], upgrade: Option[Upgrade]) {
  require(wheels.size == 4)
}

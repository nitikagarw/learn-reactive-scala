/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorRef, Props}


object Waiter {

  case class ServeCoffee(coffee: Coffee)
  case class CoffeeServed(coffee: Coffee)
  case class Complaint(coffee: Coffee)
  case class FrustratedException(coffee: Coffee, guest: ActorRef) extends IllegalStateException("Too many complaints.")

  def props(coffeeHouse: ActorRef, barista: ActorRef, maxComplaintCount: Int): Props =
    Props(new Waiter(coffeeHouse, barista, maxComplaintCount))
}

class Waiter(coffeeHouse: ActorRef, barista: ActorRef, maxComplaintCount: Int) extends Actor {

  import Waiter._

  private  var complaintCount: Int = 0

  override def receive: Receive = {
    case ServeCoffee(coffee) =>
      coffeeHouse ! CoffeeHouse.ApproveCoffee(coffee, sender())
    case Barista.CoffeePrepared(coffee, guest) => guest ! CoffeeServed(coffee)
    case Complaint(coffee) if complaintCount >= maxComplaintCount =>
      throw FrustratedException(coffee, sender())
    case Complaint(coffee) =>
      complaintCount += 1
      barista ! Barista.PrepareCoffee(coffee, sender())
  }
}

/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import akka.testkit.{ EventFilter, TestProbe }

class WaiterSpec extends BaseAkkaSpec {

  "Sending ServeCoffee to Waiter" should {
    "result in sending ApproveCoffee to CoffeeHouse" in {
      val coffeeHouse = TestProbe()
      val guest = TestProbe()
      implicit val ref = guest.ref
      val waiter = system.actorOf(Waiter.props(coffeeHouse.ref, system.deadLetters, Int.MaxValue))
      waiter ! Waiter.ServeCoffee(Coffee.Akkaccino)
      coffeeHouse.expectMsg(CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest.ref))
    }
  }

  "Sending Complaint to Waiter" should {
    "result in sending PrepareCoffee to Barista" in {
      val barista = TestProbe()
      val guest = TestProbe()
      implicit val ref = guest.ref
      val waiter = system.actorOf(Waiter.props(system.deadLetters, barista.ref, 1))
      waiter ! Waiter.Complaint(Coffee.Akkaccino)
      barista.expectMsg(Barista.PrepareCoffee(Coffee.Akkaccino, guest.ref))
    }
    "result in a FrustratedException if maxComplaintCount exceeded" in {
      val waiter = system.actorOf(Waiter.props(system.deadLetters, system.deadLetters, 0))
      EventFilter[Waiter.FrustratedException](occurrences = 1) intercept {
        waiter ! Waiter.Complaint(Coffee.Akkaccino)
      }
    }
  }
}

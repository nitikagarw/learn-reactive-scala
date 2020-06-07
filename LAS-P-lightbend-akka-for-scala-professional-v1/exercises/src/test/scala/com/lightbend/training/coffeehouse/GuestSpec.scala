/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import akka.testkit.{ EventFilter, TestProbe }
import scala.concurrent.duration.DurationInt

class GuestSpec extends BaseAkkaSpec {

  "Sending CoffeeServed to Guest" should {
    "result in increasing the coffeeCount and log a status message at info" in {
      val guest = system.actorOf(Guest.props(system.deadLetters, Coffee.Akkaccino, 100 milliseconds, Int.MaxValue))
      EventFilter.info(source = guest.path.toString, pattern = """.*[Ee]njoy.*1\.*""", occurrences = 1) intercept {
        guest ! Waiter.CoffeeServed(Coffee.Akkaccino)
      }
    }
    "result in sending ServeCoffee to Waiter after finishCoffeeDuration" in {
      val waiter = TestProbe()
      val guest = createGuest(waiter)
      waiter.within(50 milliseconds, 200 milliseconds) {
        // The timer is not extremely accurate, relax the timing constraints.
        guest ! Waiter.CoffeeServed(Coffee.Akkaccino)
        waiter.expectMsg(Waiter.ServeCoffee(Coffee.Akkaccino))
      }
    }
    "result in sending Complaint to Waiter for a wrong coffee" in {
      val waiter = TestProbe()
      val guest = createGuest(waiter)
      guest ! Waiter.CoffeeServed(Coffee.MochaPlay)
      waiter.expectMsg(Waiter.Complaint(Coffee.Akkaccino))
    }
  }

  "Sending CoffeeFinished to Guest" should {
    "result in sending ServeCoffee to Waiter" in {
      val waiter = TestProbe()
      val guest = createGuest(waiter)
      guest ! Guest.CoffeeFinished
      waiter.expectMsg(Waiter.ServeCoffee(Coffee.Akkaccino))
    }
    "result in a CaffeineException if caffeineLimit exceeded" in {
      val guest = system.actorOf(Guest.props(system.deadLetters, Coffee.Akkaccino, 100 millis, -1))
      EventFilter[Guest.CaffeineException.type](occurrences = 1) intercept {
        guest ! Guest.CoffeeFinished
      }
    }
  }

  def createGuest(waiter: TestProbe) = {
    val guest = system.actorOf(Guest.props(waiter.ref, Coffee.Akkaccino, 100 milliseconds, Int.MaxValue))
    waiter.expectMsg(Waiter.ServeCoffee(Coffee.Akkaccino)) // Creating Guest immediately sends Waiter.ServeCoffee
    guest
  }
}

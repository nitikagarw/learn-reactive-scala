package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash, Timers}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Barista {

  case class PrepareCoffee(coffee: Coffee, guest: ActorRef)
  case class CoffeePrepared(coffee: Coffee, guest: ActorRef)

  def props(prepareCoffeeDuration: FiniteDuration, accuracy: Int): Props =
    Props(new Barista(prepareCoffeeDuration, accuracy))

}

class Barista(prepareCoffeeDuration: FiniteDuration, accuracy: Int)
  extends Actor
    with ActorLogging
    with Timers
    with Stash {
  import Barista._

  override def receive: Receive = ready

  private def ready: Receive = {
    case PrepareCoffee(coffee, guest) =>
      timers.startSingleTimer("coffee-prepared", CoffeePrepared(pickCoffee(coffee), guest), prepareCoffeeDuration)
      context.become(busy(sender()))
  }

  private def busy(waiter: ActorRef): Receive = {
    case coffeePrepared: CoffeePrepared =>
      waiter ! coffeePrepared
      unstashAll()
      context.become(ready)
    case _ => stash()
  }

  private def pickCoffee(coffee: Coffee): Coffee= {
    if(Random.nextInt(100) < accuracy) coffee
    else Coffee.anyOther(coffee)
  }
}

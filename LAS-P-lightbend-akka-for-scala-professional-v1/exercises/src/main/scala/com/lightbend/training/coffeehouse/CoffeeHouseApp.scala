/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse
import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.util.Timeout

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}

object CoffeeHouseApp {

  private val opt = """(\S+)=(\S+)""".r

  def main(args: Array[String]): Unit = {
    val opts = argsToOpts(args.toList)
    applySystemProperties(opts)
    val name = opts.getOrElse("name", "coffee-house")

    val system = ActorSystem(s"$name-system")

    val statusTimeOut = system.settings.config.getDuration("coffee-house.status-timeout", TimeUnit.MILLISECONDS).millis
    val coffeeHouseApp = new CoffeeHouseApp(system)(statusTimeOut)
    coffeeHouseApp.run()
  }

  private[coffeehouse] def argsToOpts(args: Seq[String]): Map[String, String] =
    args.collect { case opt(key, value) => key -> value }.to(Map)

  private[coffeehouse] def applySystemProperties(opts: Map[String, String]): Unit =
    for ((key, value) <- opts if key startsWith "-D")
      System.setProperty(key substring 2, value)
}

class CoffeeHouseApp(system: ActorSystem)(implicit statusTimeOut: Timeout) extends Terminal {
  import system.dispatcher

  private val log = Logging(system, getClass.getName)

  private val caffeineLimit = system.settings.config.getInt("coffee-house.caffeine-limit")

  private val coffeeHouse = createCoffeeHouse()

  def run(): Unit = {
    log.warning(f"{} running%nEnter "
      + Console.BLUE + "commands" + Console.RESET
      + " into the terminal: "
      + Console.BLUE + "[e.g. `q` or `quit`]" + Console.RESET, getClass.getSimpleName)
    commandLoop()
    Await.ready(system.whenTerminated, Duration.Inf)
  }

  protected def createCoffeeHouse(): ActorRef =
    system.actorOf(CoffeeHouse.props(caffeineLimit), "coffee-house")

  @tailrec
  private def commandLoop(): Unit =
    Command(StdIn.readLine()) match {
      case Command.Guest(count, coffee, caffeineLimit) =>
        createGuest(count, coffee, caffeineLimit)
        commandLoop()
      case Command.Status =>
        status()
        commandLoop()
      case Command.Quit =>
        system.terminate()
      case Command.Unknown(command) =>
        log.warning("Unknown command {}!", command)
        commandLoop()
    }

  protected def createGuest(count: Int, coffee: Coffee, caffeineLimit: Int): Unit = {
    (1 to count).foreach {
      _ => coffeeHouse ! CoffeeHouse.CreateGuest(coffee, caffeineLimit)
    }
  }

  protected def status(): Unit = {
    (coffeeHouse ? CoffeeHouse.GetStatus).mapTo[CoffeeHouse.Status].onComplete {
      case Success(status) => log.info(s"Status: guest count = ${status.guestCount}")
      case Failure(error) => log.error(error, "Can't get status")
    }
  }
}

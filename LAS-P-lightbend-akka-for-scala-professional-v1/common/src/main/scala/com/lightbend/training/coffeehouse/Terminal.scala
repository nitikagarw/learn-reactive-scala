/**
 * Copyright © 2014 - 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import scala.util.parsing.combinator.RegexParsers

trait Terminal {

  protected sealed trait Command

  protected object Command {

    case class Guest(count: Int, coffee: Coffee, caffeineLimit: Int) extends Command

    case object Status extends Command

    case object Quit extends Command

    case class Unknown(command: String) extends Command

    def apply(command: String): Command =
      CommandParser.parseAsCommand(command)
  }

  private object CommandParser extends RegexParsers {

    def parseAsCommand(s: String): Command =
      parseAll(parser, s) match {
        case Success(command, _) => command
        case _                   => Command.Unknown(s)
      }

    def createGuest: Parser[Command.Guest] =
      opt(int) ~ ("guest|g".r ~> opt(coffee) ~ opt(int)) ^^ {
        case count ~ (coffee ~ caffeineLimit) =>
          Command.Guest(
            count getOrElse 1,
            coffee getOrElse Coffee.Akkaccino,
            caffeineLimit getOrElse Int.MaxValue
          )
      }

    def getStatus: Parser[Command.Status.type] =
      "status|s".r ^^ (_ => Command.Status)

    def quit: Parser[Command.Quit.type] =
      "quit|q".r ^^ (_ => Command.Quit)

    def coffee: Parser[Coffee] =
      "A|a|M|m|C|c".r ^^ Coffee.apply

    def int: Parser[Int] =
      """\d+""".r ^^ (_.toInt)
  }

  private val parser: CommandParser.Parser[Command] =
    CommandParser.createGuest | CommandParser.getStatus | CommandParser.quit
}

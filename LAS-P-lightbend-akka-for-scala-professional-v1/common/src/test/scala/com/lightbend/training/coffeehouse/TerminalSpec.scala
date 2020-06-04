/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

class TerminalSpec extends BaseSpec with Terminal {

  "Calling Command.apply" should {
    "create the correct CreateGuest command for the given input" in {
      Command("guest") should ===(Command.Guest(1, Coffee.Akkaccino, Int.MaxValue))
      Command("2 g") should ===(Command.Guest(2, Coffee.Akkaccino, Int.MaxValue))
      Command("g m") should ===(Command.Guest(1, Coffee.MochaPlay, Int.MaxValue))
      Command("g 1") should ===(Command.Guest(1, Coffee.Akkaccino, 1))
      Command("2 g m 1") should ===(Command.Guest(2, Coffee.MochaPlay, 1))
    }
    "create the GetStatus command for the given input" in {
      Command("status") should ===(Command.Status)
      Command("s") should ===(Command.Status)
    }
    "create the Quit command for the given input" in {
      Command("quit") should ===(Command.Quit)
      Command("q") should ===(Command.Quit)
    }
    "create the Unknown command for illegal input" in {
      Command("foo") should ===(Command.Unknown("foo"))
    }
  }
}

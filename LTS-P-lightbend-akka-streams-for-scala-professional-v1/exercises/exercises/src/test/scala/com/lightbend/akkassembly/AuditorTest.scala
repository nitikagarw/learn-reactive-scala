package com.lightbend.akkassembly

import akka.stream.scaladsl.Source
import akka.testkit.EventFilter
import org.scalatest.FreeSpec

class AuditorTest extends FreeSpec with AkkaSpec {

  "count" - {
    "should return zero if the stream is empty" in {
      val auditor = new Auditor

      val count = Source.empty[Int].runWith(auditor.count).futureValue

      assert(count === 0)
    }
    "should count elements in the stream" in {
      val auditor = new Auditor

      val count = Source(1 to 10).runWith(auditor.count).futureValue

      assert(count === 10)
    }
  }

  "log" - {
    "should log nothing if the source is empty." in {
      implicit val adapter = system.log
      val auditor = new Auditor

      EventFilter.debug(occurrences = 0).intercept {
        Source.empty[Int].runWith(auditor.log)
      }
    }
    "should log all elements to the logging adapter." in {
      implicit val adapter = system.log
      val auditor = new Auditor

      EventFilter.debug(occurrences = 10).intercept {
        Source(1 to 10).runWith(auditor.log)
      }
    }
    "should log the exact element" in {
      implicit val adapter = system.log
      val auditor = new Auditor

      EventFilter.debug(occurrences = 1, message = "Message").intercept {
        Source.single("Message").runWith(auditor.log)
      }
    }
  }
}

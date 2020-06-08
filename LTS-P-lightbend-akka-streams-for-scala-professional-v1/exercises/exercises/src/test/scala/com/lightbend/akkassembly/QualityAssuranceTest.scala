package com.lightbend.akkassembly

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.FreeSpec

import scala.collection.immutable.Seq

class QualityAssuranceTest extends FreeSpec with AkkaSpec {

  "inspect" - {
    "should reject cars with no Color" in {
      val qa = new QualityAssurance()

      val cars = Source.single(
        UnfinishedCar(
          color = None,
          engine = Some(Engine()),
          wheels = Seq.fill(4)(Wheel())
        )
      )

      cars.via(qa.inspect)
        .runWith(TestSink.probe[Car])
        .request(10)
        .expectComplete()
    }
    "should reject cars with no Engine" in {
      val qa = new QualityAssurance()

      val cars = Source.single(
        UnfinishedCar(
          color = Some(Color("000000")),
          engine = None,
          wheels = Seq.fill(4)(Wheel())
        )
      )

      cars.via(qa.inspect)
        .runWith(TestSink.probe[Car])
        .request(10)
        .expectComplete()
    }
    "should reject cars with no Wheels" in {
      val qa = new QualityAssurance()

      val cars = Source.single(
        UnfinishedCar(
          color = Some(Color("000000")),
          engine = Some(Engine()),
          wheels = Seq.empty
        )
      )

      cars.via(qa.inspect)
        .runWith(TestSink.probe[Car])
        .request(10)
        .expectComplete()
    }
    "should accept cars that are complete" in {
      val qa = new QualityAssurance()

      val completeCar = UnfinishedCar(
        color = Some(Color("000000")),
        engine = Some(Engine()),
        wheels = Seq.fill(4)(Wheel())
      )
      val incompleteCar = UnfinishedCar()

      val cars = Source(Seq(completeCar, completeCar, incompleteCar, completeCar))

      val sink = cars.via(qa.inspect).runWith(TestSink.probe[Car])
      sink.request(10)
      sink.expectNextN(3)
      sink.expectComplete()
    }
  }
}

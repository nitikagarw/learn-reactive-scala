package lectures

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

object AdvancedBackpressure extends App {

  implicit val system: ActorSystem = ActorSystem("AdvancedBackpressure")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // control backpressure
  val controlledFlow =
    Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, nInstances: Int = 1)
  case class Notification(email: String, pagerEvent: PagerEvent)

  val events =
    List(
      PagerEvent("Service Discovery Failed", new Date),
      PagerEvent("Illegal elements", new Date),
      PagerEvent("Number of HTTP 500 Spiked", new Date),
      PagerEvent("A service stopped responding", new Date)
    )

  val eventSource = Source(events)

  val onCallEngineer = "daniel@rockthejvm.com" // a fast service for fetching on-call emails

  //actually send an email
  def sendEmail(notification: Notification): Unit =
    println(
      s"Dear ${notification.email}, you have an event: ${notification.pagerEvent}"
    )

  val notificationSink =
    Flow[PagerEvent]
      .map(event => Notification(onCallEngineer, event))
      .to(Sink.foreach[Notification](sendEmail))

  //Standard
//  eventSource.to(notificationSink).run()

  /**
    * un-backpressurable source
    *
    */
  def sendEmailSlow(notification: Notification): Unit = {
    Thread.sleep(1000)
    println(
      s"Dear ${notification.email}, you have an event: ${notification.pagerEvent}"
    )
  }

  val aggregateNotificationFlow =
    Flow[PagerEvent]
      .conflate((event1, event2) => {
        val nInstances = event1.nInstances + event2.nInstances
        PagerEvent(
          s"You have $nInstances events that require your attention",
          new Date,
          nInstances
        )
      })
      .map(event => Notification(onCallEngineer, event))

  eventSource
    .via(aggregateNotificationFlow)
    .async
    .to(Sink.foreach[Notification](sendEmailSlow))
//    .run()
  //alternative to backpressure

  /**
    * Slow producers: extrapolate/expand
    */
  import scala.concurrent.duration._
  val slowCounter = Source(Stream.from(1)).throttle(1, 1 second)
  val hungrySink = Sink.foreach[Int](println)
  val extrapolator = Flow[Int].extrapolate(element => Iterator.from(element))
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))
  val expander = Flow[Int].expand(element => Iterator.from(element))

  slowCounter.via(extrapolator).to(hungrySink).run()

}

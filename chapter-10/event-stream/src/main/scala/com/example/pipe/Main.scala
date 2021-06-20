package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.example.pipe.subscribe.{NotificationSubscriber, OrderSubscriber}

import java.util.concurrent.TimeUnit

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val ec = system.dispatcher
    implicit val timeout = Timeout(10, TimeUnit.SECONDS)

    val orderSubscriber = system.actorOf(Props(new OrderSubscriber), "orderSubscriber")
    val notificationSubscriber = system.actorOf(Props(new NotificationSubscriber), "notificationSubscriber")

    system.eventStream.subscribe(orderSubscriber, classOf[SubmitOrder])
    system.eventStream.subscribe(notificationSubscriber, classOf[Order])

    system.eventStream.publish(SubmitOrder(1, "sample"))
    system.eventStream.publish(SendOrder(1, "sample"))
  }
}

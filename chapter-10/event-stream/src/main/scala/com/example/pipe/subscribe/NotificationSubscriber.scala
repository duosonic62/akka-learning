package com.example.pipe.subscribe

import akka.actor.Actor
import com.example.pipe.{SendOrder, SubmitOrder}

class NotificationSubscriber extends Actor {
  override def receive: Receive = {
    case order: SubmitOrder => println(s"notification! ordered: $order")
    case order: SendOrder => println(s"notification! send: $order")
  }
}

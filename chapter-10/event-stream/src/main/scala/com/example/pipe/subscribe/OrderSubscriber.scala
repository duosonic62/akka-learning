package com.example.pipe.subscribe

import akka.actor.Actor
import com.example.pipe.Order

class OrderSubscriber extends Actor {
  override def receive: Receive = {
    case order: Order => println(s"ordered: $order")
  }
}

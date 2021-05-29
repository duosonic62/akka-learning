package com.`akka-test`.twoway

import akka.actor.Actor

class TwoWayActor extends Actor {

  override def receive: Receive = {
      case msg => 
        sender() ! msg
  }
}
package com.`akka-test`.sideeffect

import akka.actor.ActorLogging
import akka.actor.Actor

case class Greeting(message: String)

class SideEffectingActor extends Actor with ActorLogging {
    def receive = {
        case Greeting(message) => log.info(s"Hello $message!")
    }
}
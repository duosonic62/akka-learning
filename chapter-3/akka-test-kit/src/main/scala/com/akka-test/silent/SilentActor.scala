package com.`akka-test`.silent

import akka.actor.Actor
import akka.actor.ActorRef

object SilentActor {
    case class SilentMessage(data: String)
    case class GetState(receiver: ActorRef)
}

class SilentActor extends Actor {
    import SilentActor._
    var internalState = Vector[String]()

    def receive = {
        case SilentMessage(data) =>
            internalState = internalState :+ data
        case GetState(receiver) => receiver ! internalState
    }

    def state = internalState
}
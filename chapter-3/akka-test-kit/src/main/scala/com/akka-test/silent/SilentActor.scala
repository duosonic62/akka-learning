package com.`akka-test`.silent

import akka.actor.Actor

class SilentActor extends Actor {
    def receive = {
        case message => 
    }
}
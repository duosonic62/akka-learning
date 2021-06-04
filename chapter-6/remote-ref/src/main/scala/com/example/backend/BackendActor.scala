package com.example.backend

import akka.actor.Actor

object BackendActor {
  val name = "simple-backend"
}

class BackendActor extends Actor {
  override def receive: Receive = {
    case m => println(s"received $m!")
  }
}

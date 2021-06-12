package com.example.pipe

import akka.actor.{Actor, ActorRef}

import java.time. LocalDateTime}

class GetTime(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: PhotoMessage => pipe ! msg.copy(creationTIme = Some(LocalDateTime.now()))
  }
}

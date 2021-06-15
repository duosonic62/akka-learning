package com.example.pipe

import akka.actor.{Actor, ActorRef}

class GetSpeed(pipe: ActorRef) extends Actor{
  override def receive: Receive = {
    case msg: PhotoMessage => pipe ! msg.copy(speed = getSpeed)
  }

  private def getSpeed: Option[Int] = Some(23)
}

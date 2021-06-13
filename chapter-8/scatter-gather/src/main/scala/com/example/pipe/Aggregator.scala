package com.example.pipe

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration

class Aggregator(timeout: Duration, pipe: ActorRef) extends Actor {
  val messages = new ListBuffer[PhotoMessage]

  override def receive: Receive = {
    case rcvMsg: PhotoMessage =>
      messages.find(_.id == rcvMsg.id) match {
        case Some(alreadyRcvMsg) =>
          val newCombinedMsg = PhotoMessage(
            rcvMsg.id,
            rcvMsg.photo,
            rcvMsg.creationTIme.orElse(alreadyRcvMsg.creationTIme),
            rcvMsg.speed.orElse(alreadyRcvMsg.speed)
          )
          pipe ! newCombinedMsg
          messages -= alreadyRcvMsg
        case None => messages += rcvMsg
      }
  }
}

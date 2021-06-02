package com.goticks

import akka.actor.Actor
import akka.util.Timeout
import akka.actor.ActorRef
import akka.actor.Props

object TicketActor {
  def props(implicit timeout: Timeout) = Props(new TicketActor())

  def name = "ticketActor"

  case class GetTicketInfo(id: Int)

  case class TicketInfo(id: Int, name: String)
}

class TicketActor(implicit timeout: Timeout) extends Actor {

  import TicketActor._

  override def receive: Receive = {
    case GetTicketInfo(id) => sender() ! TicketActor.TicketInfo(id, "hoge")
  }
}

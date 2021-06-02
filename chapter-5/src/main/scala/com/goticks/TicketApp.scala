package com.goticks

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit

object TicketApp extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  val ticketActor = system.actorOf(TicketActor.props, TicketActor.name)
  val userActor = system.actorOf(UserActor.props, UserActor.name)

  val info = for {
    ticket <- ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
    user <- userActor.ask(UserActor.GetUser(3)).mapTo[UserActor.User]
  } yield UserTicket(
    userId = user.id,
    userName = user.name,
    ticketId = ticket.id,
    ticketName = ticket.name
  )

  info.foreach(println)
}

case class UserTicket(userId: Int, userName: String, ticketId: Int, ticketName: String)
package com.goticks

import akka.util.Timeout
import akka.actor.Actor
import akka.actor.Props

object UserActor {
  def props(implicit timeout: Timeout) = Props(new UserActor())

  def name = "userActor"

  case class GetUser(id: Int)

  case class User(id: Int, name: String)
}

class UserActor(implicit timeout: Timeout) extends Actor {

  import UserActor._

  override def receive: Receive = {
    case GetUser(id) => sender() ! User(id, "sample name")
  }
}
package com.example.frontend

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object FrontendApp extends App {
  val config = ConfigFactory.load("frontend")
  val frontend = ActorSystem("frontend", config)

  val path = "akka://simple-backend@127.0.0.1:2551/user/simple-backend"
  val backendActor = frontend.actorSelection(path)

  backendActor ! "Hello Remote World"
}

package com.example.frontend

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object FrontendApp extends App {
  val config = ConfigFactory.load("frontend")
  val frontend = ActorSystem("frontend", config)

  val path = "akka://simple-backend@127.0.0.1:2551/user/simple-backend"
//  val backendActor = frontend.actorSelection(path)
  val backendActor = frontend.actorOf(Props(new RemoteLookupProxy(path)), "lookupBackend")

  backendActor ! "Hello Remote World"
}

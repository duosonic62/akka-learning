package com.example.frontend

import akka.actor.{ActorRef, ActorSystem, Props}
import com.example.backend.BackendActor
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor

object FrontendApp extends App {
  val config = ConfigFactory.load("frontend")
  implicit val system: ActorSystem = ActorSystem("frontend", config)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val backendActor: ActorRef = system.actorOf(Props[BackendActor], BackendActor.name)

  backendActor ! "Hello Remote World"
}

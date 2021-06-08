package com.example.backend

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

object BackendApp extends App {
  val config = ConfigFactory.load("backend")
  implicit val system: ActorSystem = ActorSystem(BackendActor.name, config)

  // ここではアクターを作成しない (フロントからデプロイするため)
//  implicit val ec: ExecutionContextExecutor = system.dispatcher
//  system.actorOf(Props[BackendActor], BackendActor.name)
}
package com.example.backend

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

object BackendApp extends App with  RequestTimeout{
  val config = ConfigFactory.load("backend")

  implicit val system: ActorSystem = ActorSystem(BackendActor.name, config)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  system.actorOf(Props[BackendActor], BackendActor.name)
}
trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
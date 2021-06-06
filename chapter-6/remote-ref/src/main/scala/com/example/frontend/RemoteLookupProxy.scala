package com.example.frontend

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, ReceiveTimeout, Terminated}

import scala.concurrent.duration._
import scala.language.postfixOps

class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path) // 全てのアクターが認識できる `Identify` メッセージの送信を試みる
  }

  override def receive: Receive = identify

  def identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state")
      context.become(active(actor))
      context.watch(actor)

    case ActorIdentity(`path`, None) =>
      log.error(s"Reomote actor with path $path is not available")

    case ReceiveTimeout =>
      sendIdentifyRequest()

    case msg: Any =>
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef: ActorRef) =>
      log.info(s"Actor $actorRef terminated.")
      context.become(identify)
      log.info("switching to identity state")
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest()

    case msg: Any => actor forward msg
  }
}

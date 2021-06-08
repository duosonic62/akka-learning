package com.example.frontend

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, ReceiveTimeout, Terminated}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * リモートアクターの死活監視をするプロキシ
 *
 * @param path リモートアクターのパス
 */
class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)
  sendIdentifyRequest()

  /**
   * リモートアクターに Identify メッセージを送る
   */
  def sendIdentifyRequest(): Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path) // 全てのアクターが認識できる `Identify` メッセージの送信を試みる
  }

  // becomeメソッドが呼び出されると `identify` と `active` のメッセージハンドラーが入れ替わる
  override def receive: Receive = identify

  def identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) => // リモートアクターからアクティブだとメッセージが来た場合
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state")
      // activeにメッセージハンドラを入れ替え
      context.become(active(actor))
      context.watch(actor)

    case ActorIdentity(`path`, None) => // リモートアクターから利用不可とメッセージが来た時
      log.error(s"Remote actor with path $path is not available")

    case ReceiveTimeout => // timeout
      sendIdentifyRequest()

    case msg: Any => // unexpected message... error?
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef: ActorRef) => // リモートアクターが終了した時
      log.info(s"Actor $actorRef terminated.")
      // identifyにメッセージハンドラを入れ替え
      context.become(identify)
      log.info("switching to identity state")
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest()

    case msg: Any => actor forward msg
  }
}

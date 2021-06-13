package com.example.pipe

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

case class TimeoutMessage(msg: PhotoMessage)

class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor {
  val messages = new ListBuffer[PhotoMessage]
  implicit val ec = context.system.dispatcher

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    // リスタート前にメッセージをメールボックスに送っておく
    messages.foreach(self ! _)
    messages.clear()
  }

  override def receive: Receive = {
    case rcvMsg: PhotoMessage =>
      messages.find(_.id == rcvMsg.id) match {
        case Some(alreadyRcvMsg) =>
          val newCombinedMsg = PhotoMessage(
            rcvMsg.id,
            rcvMsg.photo,
            rcvMsg.creationTIme.orElse(alreadyRcvMsg.creationTIme),
            rcvMsg.speed.orElse(alreadyRcvMsg.speed)
          )
          pipe ! newCombinedMsg
          messages -= alreadyRcvMsg
        case None =>
          messages += rcvMsg
          // タイムアウトメッセージをスケジュール
          context.system.scheduler.scheduleOnce(
            timeout,
            self,
            new TimeoutMessage(rcvMsg)
          )
      }
    case TimeoutMessage(rcvMsg) =>
      messages.find(_.id == rcvMsg.id) match {
        case Some(alreadyRcvMsg) =>
          // 結合前のメッセージをそのまま送信
          pipe ! alreadyRcvMsg
          messages -= alreadyRcvMsg
        case None => // 特に何もしない
      }
    case ex: Exception => throw ex // 例外が来たら再起動
  }
}

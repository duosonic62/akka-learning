package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ScatterGatherPatternSpec  extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "ScatterGather" should {
    "send and received massages" in {
      val endProbe = TestProbe()
      val now = LocalDateTime.now
      val aggregatorRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref))
      )
      val speedRef = system.actorOf(
        Props(new GetSpeed(aggregatorRef))
      )
      val timeRef = system.actorOf(
        Props(new GetTime(aggregatorRef, now))
      )
      val actorRef = system.actorOf(
        Props(new RecipientList(Seq(speedRef, timeRef)))
      )

      val msg = PhotoMessage(
        "id1",
        "message"
      )
      actorRef ! msg

      val combinedMsg = PhotoMessage(msg.id, msg.photo, Some(now), Some(23))
      endProbe.expectMsg(combinedMsg)
    }
  }
}

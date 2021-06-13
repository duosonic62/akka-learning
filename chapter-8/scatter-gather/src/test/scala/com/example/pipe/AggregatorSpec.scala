package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class AggregatorSpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "Aggregator" should {
    "combined messages" in {
      val endProbe = TestProbe()
      val actorRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref))
      )
      val now = LocalDateTime.now

      val msg1 = PhotoMessage(
        "id1",
        "message",
        Some(now),
        None
      )
      actorRef ! msg1

      val msg2 = PhotoMessage(
        "id1",
        "message",
        None,
        Some(60)
      )
      actorRef ! msg2

      val combinedMsg = PhotoMessage(
        "id1",
        "message",
        Some(now),
        Some(60)
      )

      endProbe.expectMsg(combinedMsg)
    }

    "if timeout, send not combined messages" in {
      val endProbe = TestProbe()
      val now = LocalDateTime.now
      val actorRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref))
      )
      val msg1 = PhotoMessage(
        "id1",
        "message",
        Some(now),
        None
      )

      actorRef ! msg1
      endProbe.expectMsg(msg1)
    }

    "if restart, not combined messages is sent mailbox" in {
      val endProbe = TestProbe()
      val now = LocalDateTime.now
      val actorRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref))
      )
      val msg1 = PhotoMessage(
        "id1",
        "message",
        Some(now),
        None
      )
      actorRef ! msg1

      actorRef ! new IllegalStateException("restart")

      val msg2 = PhotoMessage(
        "id1",
        "message",
        None,
        Some(60)
      )
      actorRef ! msg2

      val combinedMsg = PhotoMessage(
        "id1",
        "message",
        Some(now),
        Some(60)
      )

      endProbe.expectMsg(combinedMsg)
    }
  }

}


package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RecipientListSpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "RecipientList" should {
    "send messages to all actors" in {
      val msg = "message"
      val endProbe1 = TestProbe()
      val endProbe2 = TestProbe()
      val endProbe3 = TestProbe()
      val actorRef = system.actorOf(Props(new RecipientList(Seq(endProbe1.ref, endProbe2.ref, endProbe3.ref))))

      actorRef ! msg
      endProbe1.expectMsg(msg)
      endProbe2.expectMsg(msg)
      endProbe3.expectMsg(msg)
    }
  }

}


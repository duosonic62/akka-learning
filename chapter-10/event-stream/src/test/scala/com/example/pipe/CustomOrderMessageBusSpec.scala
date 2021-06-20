package com.example.pipe

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class CustomOrderMessageBusSpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "CustomOrderMessageBus" should {
    "publish event" in {
      val bus = new CustomOrderMessageBus

      val single = TestProbe()
      bus.subscribe(single.ref, false)

      val multi = TestProbe()
      bus.subscribe(multi.ref, true)

      val msg = SubmitOrder(1, "single")
      bus.publish(msg)
      single.expectMsg(msg)
      multi.expectNoMessage(3 seconds)

      val msg2 = SubmitOrder(2, "multi")
      bus.publish(msg)
      multi.expectMsg(msg)
      single.expectNoMessage(3 seconds)
    }
  }
}

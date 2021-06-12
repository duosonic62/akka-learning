package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PipelineSpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "LicenseFilter" should {
    "not filtered fill license" in {
      val endProbe = TestProbe()
      val speedFilter = system.actorOf(
        Props(new GetSpeed(50, endProbe.ref))
      )
      val licenseFilter = system.actorOf(
        Props(new GetTime(speedFilter))
      )

      val msg = Photo("1234", 60)
      licenseFilter ! msg
      endProbe.expectMsg(msg)

      licenseFilter ! Photo("", 60)
      endProbe.expectNoMessage()

      licenseFilter ! Photo("1234", 49)
      endProbe.expectNoMessage()
    }
  }
}

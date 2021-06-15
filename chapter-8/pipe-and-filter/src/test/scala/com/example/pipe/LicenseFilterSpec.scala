package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class LicenseFilterSpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "LicenseFilter" should {
    "not filtered fill license" in {
      val photo = Photo("license", 100)
      val speedFilter = system.actorOf(
        Props(new LicenseFilter(testActor))
      )

      speedFilter ! photo

      expectMsg(photo)
    }

    "filtered not fill license" in {
      val photo = Photo("", 100)
      val speedFilter = system.actorOf(
        Props(new LicenseFilter(testActor))
      )

      speedFilter ! photo

      expectNoMessage()
    }
  }

}


package com.example.pipe

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

class SlipRouterPatternSpec  extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "SlipRouter" should {
    "minimal order" in {
      val probe = TestProbe()
      val router = system.actorOf(Props(new SlipRouter(probe.ref)), "SlipRouter1")

      val minimalOrder = new Order(Seq())
      router ! minimalOrder
      val defaultCar = new Car(color = "black")
      probe.expectMsg(defaultCar)
    }

    "fullOrder" in {
      val probe = TestProbe()
      val router = system.actorOf(Props(new SlipRouter(probe.ref)), "SlipRouter2")

      val fullOrder = new Order(Seq(
        CarOptions.CAR_COLOR_GRAY,
        CarOptions.PARKING_SENSORS,
        CarOptions.NAVIGATION
      ))
      router ! fullOrder
      val carWithFullOptions = new Car(color = "gray", hasNavigation = true, hasParkingSensors = true)
      probe.expectMsg(carWithFullOptions)
    }
  }
}

package com.example.persistent

import akka.actor.{ActorSystem, PoisonPill}

class CalculatorSpec extends PersistenceSpec(ActorSystem("test")) with PersistenceCleanup {
  "The Calculator" should {
    "recover last known result after crash" in {
      val calc = system.actorOf(Calculator.props, Calculator.name)
      var cmd = Add(1)
      calc ! cmd
      calc ! GetResult
      expectMsg(1d)

      calc ! Subtract(0.5d)
      calc ! GetResult
      expectMsg(0.5d)

      killActors(calc)

      val calcRestarted = system.actorOf(Calculator.props, Calculator.name)
      calcRestarted ! GetResult
      expectMsg(0.5d)

      calcRestarted ! Add(1d)
      calcRestarted ! GetResult
      expectMsg(1.5d)
    }
  }

}

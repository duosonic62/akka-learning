package com.`akka-test`.twoway

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike
import akka.testkit.ImplicitSender
import com.goticks.StopSystemAfterAll
import akka.actor.Props

class TwoWayActorTest extends TestKit(ActorSystem("testsystem"))
    with AnyWordSpecLike
    with ImplicitSender
    with StopSystemAfterAll {
    
    "Reply with the same message if recievrs without ask" in {
        val echo = system.actorOf(Props[TwoWayActor], "echo2")
        echo ! "some message"
        expectMsg("some message")
    } 

}
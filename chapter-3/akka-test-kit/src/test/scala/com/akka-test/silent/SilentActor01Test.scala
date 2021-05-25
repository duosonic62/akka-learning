package com.`akka-test`.silent

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.must.Matchers
import com.goticks.StopSystemAfterAll
import akka.testkit.TestActor
import akka.testkit.TestActorRef
import akka.actor.Props

class SilentActor01Test extends TestKit(ActorSystem("testsystem"))
    with AnyWordSpecLike
    with Matchers
    with StopSystemAfterAll {
    
        // SilentActor
        "A Silent Actor" must {
            "change state when if recieves message, single threaded" in {
                import SilentActor._
                val silentActor = TestActorRef[SilentActor]
                
                silentActor ! SilentMessage("whisper")
                silentActor.underlyingActor.state must contain("whisper")
            }

            "change state when if recieves message, multi threaded" in {
                import SilentActor._
                val silentActor = system.actorOf(Props[SilentActor], "s3")
                silentActor ! SilentMessage("whisper 1")
                silentActor ! SilentMessage("whisper 2")

                silentActor ! GetState(testActor) // testKitに含まれているtestActorにメッセージを送らせちゃう
                expectMsg(Vector("whisper 1", "whisper 2"))
            }
        }
}
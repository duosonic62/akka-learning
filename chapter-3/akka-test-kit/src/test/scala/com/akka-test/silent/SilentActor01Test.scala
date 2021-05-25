package com.`akka-test`

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.must.Matchers
import com.goticks.StopSystemAfterAll

class SilentActor01Test extends TestKit(ActorSystem("testsystem"))
    with AnyWordSpecLike
    with Matchers
    with StopSystemAfterAll {
    
        // SilentActor
        "A Silent Actor" must {
            "change state when if trcives message, single threaded" in {
                fail("not implemented yet")
            }

            "change state when if trcives message, multi threaded" in {
                fail("not implemented yet")
            }
        }
}
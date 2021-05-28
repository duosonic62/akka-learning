package com.`akka-test`.sending

import akka.testkit.TestKit
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.must.Matchers
import akka.actor.ActorSystem
import com.goticks.StopSystemAfterAll
import scala.util.Random

class SendingActorTest extends TestKit(ActorSystem("testsystem"))
    with AnyWordSpecLike
    with Matchers
    with StopSystemAfterAll {

        // SendingActor
        "A Sending Actor" must {
            "send a message to another actor when it has finished processing" in {
                import SendingActor._
                val props = SendingActor.props(testActor)
                val sendingActor = system.actorOf(props, "sendingActor")

                val size = 1000
                val maxInclusive = 100000

                def randomEvents() = (0 until size).map{_ =>
                    Event(Random.nextInt(maxInclusive))
                }.toVector

                val unsorted = randomEvents()
                val sortEvents =  SortEvents(unsorted)
                sendingActor ! sortEvents

                // 内部に状態を持たないのでイベントの発生を取得する
                expectMsgPF() {
                    case SortedEvents(events) => 
                        events.size must be(size)
                        unsorted.sortBy(_.id) must be(events)
                }
            }
        }


}
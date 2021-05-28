package com.`akka-test`.sideeffect

import akka.testkit.TestKit
import org.scalatest.wordspec.AnyWordSpecLike
import com.goticks.StopSystemAfterAll
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.testkit.CallingThreadDispatcher
import akka.actor.Props
import akka.testkit.EventFilter

import SideEffectingActorTest._

class SideEffectingActorTest extends TestKit(testSystem)
 with AnyWordSpecLike
 with StopSystemAfterAll {
     "The Greeter" must {
         "say Hello Wolrd! when a Greeting(World) is sent to it" in {

            // CallingThreadDispatcherを使ってシングルスレッドにする
             val dispatchId = CallingThreadDispatcher.Id
             val props = Props[SideEffectingActor].withDispatcher(dispatchId)
             
             val actor = system.actorOf(props)

            EventFilter.info(message = "Hello World!", occurrences = 1).intercept {
                actor ! Greeting("World")
            } 
         }
     }
  
}

object SideEffectingActorTest {
    val testSystem = {
        // イベントハンドラのリスナーをテスト用の `TestEventListener` にオーバーライド
        val config = ConfigFactory.parseString(
            """
            akka.loggers = [akka.testkit.TestEventListener]
            """
        )
        ActorSystem("testsystem", config)
    }
}
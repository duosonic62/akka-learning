package com.example.pipe

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

class InventorySpec extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with Matchers
  with StopSystemAfterAll {

  "Inventory" must {
    "change states" in {
      val publisher = system.actorOf(Props(new Publisher(2, 2)))
      val inventory = system.actorOf(Props(new Inventory(publisher)))
      val stateProbe = TestProbe()
      val replyProbe = TestProbe()

      inventory ! new SubscribeTransitionCallBack(stateProbe.ref)
      stateProbe.expectMsg(new CurrentState(inventory, WaitForRequests))

      //start test
      inventory ! new BookRequest("context1", replyProbe.ref)
      stateProbe.expectMsg(new Transition(inventory, WaitForRequests, WaitForPublisher))
      stateProbe.expectMsg(new Transition(inventory, WaitForPublisher, ProcessRequest))
      stateProbe.expectMsg(new Transition(inventory, ProcessRequest, WaitForRequests))
      replyProbe.expectMsg(new BookReply("context1", Right(1)))

      inventory ! new BookRequest("context2", replyProbe.ref)
      stateProbe.expectMsg(new Transition(inventory, WaitForRequests, ProcessRequest))
      stateProbe.expectMsg(new Transition(inventory, ProcessRequest, WaitForRequests))
      replyProbe.expectMsg(new BookReply("context2", Right(2)))

      inventory ! new BookRequest("context3", replyProbe.ref)
      stateProbe.expectMsg(new Transition(inventory, WaitForRequests, WaitForPublisher))
      stateProbe.expectMsg(new Transition(inventory, WaitForPublisher, ProcessSoldOut))
      replyProbe.expectMsg(new BookReply("context3", Left("SoldOut")))
      stateProbe.expectMsg(new Transition(inventory, ProcessSoldOut, SoldOut))
    }
  }
}

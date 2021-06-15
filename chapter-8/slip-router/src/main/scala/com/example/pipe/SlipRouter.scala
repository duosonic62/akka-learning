package com.example.pipe

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ListBuffer

class SlipRouter(endStep: ActorRef) extends Actor with RouteSlip {
  val paintBlack: ActorRef = context.actorOf(Props(new PaintCar("black")), "paintBlack")
  val paintGray: ActorRef = context.actorOf(Props(new PaintCar("gray")), "paintGray")
  val addNavigation: ActorRef = context.actorOf(Props(new AddNavigation()), "addNavigation")
  val addParkingSensors: ActorRef = context.actorOf(Props(new AddParkingSensors()), "addParkingSensors")

  override def receive: Receive = {
    case order: Order =>
      val routeSlip = createRouteSlip(order.option)
      sendMessageToNextTask(routeSlip, new Car)
  }

  private def createRouteSlip(options: Seq[CarOptions.Value]): Seq[ActorRef] = {
    val routeSlip = new ListBuffer[ActorRef]

    if (!options.contains(CarOptions.CAR_COLOR_GRAY)) {
      routeSlip += paintBlack
    }

    options.foreach {
      case CarOptions.CAR_COLOR_GRAY => routeSlip += paintGray
      case CarOptions.NAVIGATION => routeSlip += addNavigation
      case CarOptions.PARKING_SENSORS => routeSlip += addParkingSensors
    }

    routeSlip += endStep
    routeSlip
  }
}

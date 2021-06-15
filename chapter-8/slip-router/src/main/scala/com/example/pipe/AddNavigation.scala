package com.example.pipe

import akka.actor.Actor

class AddNavigation extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, car: Car) =>
      sendMessageToNextTask(routeSlip, car.copy(hasNavigation = true))
  }
}
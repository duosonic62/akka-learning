package com.example.pipe

import akka.actor.Actor

class PaintCar(color: String) extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, car: Car) =>
      sendMessageToNextTask(routeSlip, car.copy(color = color))
  }
}

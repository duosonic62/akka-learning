package com.example.pipe

import akka.actor.Actor

class Publisher(totalNrBooks: Int, nrBooksPerRequest: Int)
  extends Actor {

  var nrLeft: Int = totalNrBooks
  def receive: Receive = {
    case PublisherRequest =>
      if (nrLeft == 0)
        sender() ! BookSupplySoldOut
      else {
        val supply = math.min(nrBooksPerRequest, nrLeft)
        nrLeft -= supply
        sender() ! new BookSupply(supply)
      }
  }
}
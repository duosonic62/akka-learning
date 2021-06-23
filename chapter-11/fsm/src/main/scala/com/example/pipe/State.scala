package com.example.pipe

import akka.actor.ActorRef

sealed trait State
case object WaitForRequests extends State
case object ProcessRequest extends State
case object WaitForPublisher extends State
case object SoldOut extends State
case object ProcessSoldOut extends State

case class StateData(nrBooksInStore: Int, pendingRequests: Seq[BookRequest])

sealed trait Event
case class BookRequest(context: AnyRef, target: ActorRef) extends Event
case class PendingRequests() extends Event
case class BookSupply(nrBooks: Int) extends Event
case class BookSupplySoldOut() extends Event
case class Done() extends Event
case class PublisherRequest() extends Event
case class BookReply(context: AnyRef, reserveId: Either[String, Int]) extends Event
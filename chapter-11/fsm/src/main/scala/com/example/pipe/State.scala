package com.example.pipe

sealed trait State
case object WaitForRequests extends State
case object ProcessRequest extends State
case object WaitForPublisher extends State
case object SoldOut extends State
case object ProcessSoldOut extends State

case class StateData(nrBooksInStore: Int, pendingRequests: Seq[BookRequest])

sealed trait Event
case class BookRequest() extends Event
case class PendingRequests() extends Event
case class BookSupply(nrBooks: Int) extends Event
case class BookSupplySoldOut() extends Event
case class Done() extends Event
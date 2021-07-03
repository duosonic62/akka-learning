package com.example.persistent

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}

class Calculator extends PersistentActor with ActorLogging {
  var state: CalculationResult = CalculationResult()
  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case RecoveryCompleted => log.info("Calculator recovery completed")
  }

  override def receiveCommand: Receive = {
    case Add(value)      => persist(Added(value))(updateState)
    case Subtract(value) => persist(Subtracted(value))(updateState)
    case Divide(value)   => if(value != 0) persist(Divided(value))(updateState)
    case Multiply(value) => persist(Multiplied(value))(updateState)
    case PrintResult     => println(s"the result is: ${state.result}")
    case GetResult       => sender() ! state.result
    case Clear           => persist(Reset)(updateState)
  }

  override def persistenceId: String = Calculator.name

  val updateState: Event => Unit = {
    case Reset             => state = state.reset
    case Added(value)      => state = state.add(value)
    case Subtracted(value) => state = state.subtract(value)
    case Divided(value)    => state = state.divide(value)
    case Multiplied(value) => state = state.multiply(value)
  }
}

object Calculator {
  def name = "my-calculator"
  def props = Props(new Calculator)
}
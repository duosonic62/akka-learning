package com.example.persistent

sealed trait Command
case object Clear extends Command
case class Add(value: Double) extends Command
case class Subtract(value: Double) extends Command
case class Divide(value: Double) extends Command
case class Multiply(value: Double) extends Command
case object PrintResult extends Command
case object GetResult extends Command
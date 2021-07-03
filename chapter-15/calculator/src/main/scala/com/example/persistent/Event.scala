package com.example.persistent

sealed trait Event
case object Reset extends Event
case class Added(value: Double) extends Event
case class Subtracted(value: Double) extends Event
case class Divided(value: Double) extends Event
case class Multiplied(value: Double) extends Event
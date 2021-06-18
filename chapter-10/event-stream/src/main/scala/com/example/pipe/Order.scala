package com.example.pipe

trait Order {
  def id: Int
  def name: String
}

case class SubmitOrder(id: Int, name : String) extends Order
case class SendOrder(id: Int, name : String) extends Order

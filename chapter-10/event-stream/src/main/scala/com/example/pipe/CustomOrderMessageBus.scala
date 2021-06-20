package com.example.pipe

import akka.actor.ActorRef
import akka.event.{ActorEventBus, EventBus, LookupClassification}

class CustomOrderMessageBus extends EventBus with LookupClassification with ActorEventBus {
  override type Event = Order

  // Booleanが分類子なので、mapSizeは true/false の2値
  override type Classifier = Boolean
  override def mapSize(): Int = 2

  override protected def classify(event: Order): Boolean = {
    event.id > 1
  }

  override protected def publish(event: Order, subscriber: ActorRef): Unit = {
    subscriber ! event
  }
}

package com.example.persistent

import akka.actor.ActorSystem
import com.example.persistent.Basket.ItemRemoved

class BasketSpec extends PersistenceSpec(ActorSystem("test")) with PersistenceCleanup {
  val shopperId = 2L
  val macbookPro: Item = Item("Apple Macbook Pro", 1, BigDecimal(2499.99))
  val macPro: Item = Item("Apple Mac Pro", 1, BigDecimal(10499.99))
  val displays: Item = Item("4k display", 3, BigDecimal(2499.99))
  val appleMouse: Item = Item("Apple Mouse", 1, BigDecimal(99.99))
  val appleKeyboard: Item = Item("Apple Keyboard", 1, BigDecimal(79.99))
  val dWave: Item = Item("D-Wave One", 1, BigDecimal(14999999.99))

  "The basket" should {
    "skip basket events that occurred before Cleared during recovery" in {
      val basket = system.actorOf(Basket.props, Basket.name(shopperId))
      basket ! Basket.Add(macbookPro, shopperId)
      basket ! Basket.Add(displays, shopperId)
      basket ! Basket.GetItems(shopperId)
      expectMsg(Items(macbookPro, displays))

      basket ! Basket.Clear(shopperId)

      basket ! Basket.Add(macPro, shopperId)
      basket ! Basket.RemoveItem(macPro.productId, shopperId)
      expectMsg(Some(Basket.ItemRemoved(macPro.productId)))

      basket ! Basket.Clear(shopperId)
      basket ! Basket.Add(dWave, shopperId)
      basket ! Basket.Add(displays, shopperId)

      basket ! Basket.GetItems(shopperId)
      expectMsg(Items(dWave, displays))

      killActors(basket)

      val basketResurrected = system.actorOf(Basket.props, Basket.name(shopperId))
      basketResurrected ! Basket.GetItems(shopperId)
      expectMsg(Items(dWave, displays))
      basketResurrected ! Basket.CountRecoveredEvents(shopperId)
      expectMsg(Basket.RecoveredEventsCount(2))

      killActors(basketResurrected)
    }
  }
}

package com.example.persistent

case class Items(list: List[Item]) {
  def add(newItem: Item): Items = Items.aggregate(list :+ newItem)
  def add(items: Items): Items = Items.aggregate(list ++ items.list)

  def containsProduct(productId: String): Boolean =
    list.exists(_.productId == productId)

  def removeItem(productId: String): Items =
    Items.aggregate(list.filterNot(_.productId == productId))

  def updateItem(productId: String, number: Int): Items = {
    val newList = list.find(_.productId == productId).map { item =>
      list.filterNot(_.productId == productId) :+ item.update(number)
    }.getOrElse(list)
    Items.aggregate(newList)
  }
  def clear: Items = Items()
}

case class Item(productId: String, number: Int, unitPrice: BigDecimal) {
  def aggregate(item: Item): Option[Item] = {
    if(item.productId == productId) {
      Some(copy(number = number + item.number))
    } else {
      None
    }
  }

  def update(number: Int): Item = copy(number = number)
}

object Items {
  def apply(args: Item*): Items = Items.aggregate(args.toList)
  def aggregate(list: List[Item]): Items = Items(add(list))

  private def add(list: List[Item]) = aggregateIndexed(indexed(list))
  private def indexed(list: List[Item]) = list.zipWithIndex

  private def aggregateIndexed(indexed: List[(Item, Int)]) = {
    def grouped = indexed.groupBy {
      case (item, _) => item.productId
    }
    def reduced = grouped.flatMap { case (_, groupedIndexed) =>
      val init = (Option.empty[Item],Int.MaxValue)
      val (item, ix) = groupedIndexed.foldLeft(init) {
        case ((accItem, accIx), (item, ix)) =>
          val aggregated =
            accItem.map(i => item.aggregate(i))
              .getOrElse(Some(item))

          (aggregated, Math.min(accIx, ix))
      }

      item.filter(_.number > 0)
        .map(i => (i, ix))
    }
    def sorted = reduced.toList
      .sortBy { case (_, index) => index}
      .map { case (item, _) => item}
    sorted
  }
}
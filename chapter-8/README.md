# アクターの構造パターン

## パイプ & フィルターパターン
フィルター(制約)をパイプに接続することにより、イベントが発生した場合に一連のタスクをこなす。  
Akkaではアクターを使ってフィルターを実装する。すでにメッセージングの仕組みがあるので、複数のアクターを接続すると一連の処理のパイプを組むことができる。

ただし以下の条件を満たしている必要がある。これはフィルターの追加や適用順の変更、削除を容易にするためである。

* インターフェースが全てのフィルター(アクター)で同じであること
* 全てのアクターが独立していること

下記のケースではLicenseFilterとSpeedFilterの両方で、 `Photo` メッセージを処理するようにしておくとフィルターとしてしようできる。

```scala
class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor{
  override def receive: Receive = {
    case msg: Photo =>
      if (msg.speed > minSpeed) pipe ! msg
  }
}

class LicenseFilter(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: Photo =>
      if (msg.license.nonEmpty) pipe ! msg
  }
}
```

フィルターを適用する場合は、引数として次のフィルターをパイプとして渡す。
下記の例では `入力 -> licenceFilter -> speedFilter -> end` の順番で処理が行われる。

```scala
      val speedFilter = system.actorOf(
        Props(new SpeedFilter(50, end))
      )
      val licenseFilter = system.actorOf(
        Props(new LicenseFilter(speedFilter))
      )
```

また順番の入れ替えは容易で、各フィルターに渡す引数を変更すれば実現できる。

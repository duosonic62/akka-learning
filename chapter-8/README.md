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

## スキャッタギャザーパターン
タスクを並行実行して処理を行うパターン。  
処理タスクはギャザー部分でAkkaが提供するアグリゲータを使用する。
受信者リストはスキャッタコンポーネント部分となる。

スキャッタでは複数のパイプ(処理)にメッセージを送るため、送信先のリストを受け取る。
一つのメッセージを受け取って送信先のリスト全てに送信する。

```scala
class RecipientList(recipientList: Seq[ActorRef]) extends Actor {
  override def receive: Receive = {
    case msg: AnyRef => recipientList.foreach(_ ! msg)
  }
}
```

ギャザーでは、複数のパイプの処理を合成して一つのメッセージにまとめる。
複数のメッセージを合成する場合は、メッセージが揃うまで待つように状態を持たなくてはならない。

```scala
class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor {
  val messages = new ListBuffer[PhotoMessage]

  override def receive: Receive = {
    case rcvMsg: PhotoMessage =>
      messages.find(_.id == rcvMsg.id) match {
        // メッセージが存在すれば合成   
        case Some(alreadyRcvMsg) =>
          val newCombinedMsg = rcvMsg + alreadyRcvMsg
          pipe ! newCombinedMsg
          messages -= alreadyRcvMsg
          
        // 存在しなければ保管庫にためる  
        case None => messages += rcvMsg
      }
  }
}
```

また、ギャザー部分ではアクターの再起動時の合成できていないメッセージやタイムアウト等の考慮が必要となる。

このパターンではメッセージを加工して次のパイプに処理をつなぐことがある。副作用を許容するクラスをメッセージにしてしまうと、並行処理を行うため意図しない動作をしてしまうことがある。  
そのため、 `case class` を使用するなど、イミュータブルなオブジェクトをメッセージにするようにすると良い。

### 競争タスク
複数の同様の処理を行うタスクを並行で処理し、その結果を比較してフィルターした結果を次の処理に回すパターン。
最安値や、最速で応答のあった処理を使ったりする感じ。

### 並列強調処理
複数の処理を並行で行い、その結果を一つのメッセージにマージするパターン。
複数のデータを取得して一つにマージして使ったりする感じ。

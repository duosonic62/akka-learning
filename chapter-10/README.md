# Message Channel
チャネルとはアクターが別のアクターにメッセージを送信する送信方法のことを指す。

## Point to Point Channel
一般的な一対一のチャネル。下記のように、 `受け取るアクターを指定して` メッセージを送信するチャネルをPoint to Point Channelという。  
ラウンドロビンルーターのように複数の受信者がいたとしても、 `一つのメッセージが一つのアクターに配信されて` いればこのモデルとなる。

```scala
class EchoActor(sendActor: ActorRef) extends Actor {
  override def receive = {
    case msg: Msg => senderActor ! msg // senderActor
  }
}
```


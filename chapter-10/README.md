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

## Publish Subscribe Channel
`送信者が受信者を知らずにメッセージを送信するチャネル` のことを Publish Subscribe Channelという。  
メッセージを必要とする受信アクターを管理する責務はチャネルが持つ。また、複数の受信者に一斉に配信することもできる。

### チャネルのサブスクライブ
特定のメッセージを受け取るには受信者がチャネルをサブスクライブする必要がある。  
サブスクライブは下記のように登録します。receiverは `受信するActorRef` を classOf[Msg] にはサブスクライブするメッセージの型を指定する。

```scala
sysytem.eventStream.subscribe(receiver, classOf[Msg])
```

### チャネルのパブリッシュ
このチャネルでは特に受信者を特定できないため、直接アクターを指定するのではなくeventStreamを通してメッセージを送信する。

```scala
system.eventStream.publish(msg)
```

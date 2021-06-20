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

### カスタムイベントバス
EventBusトレイトを実装することで、パブリックサブスクライブチャネルを実装することができる。

* Event  
  パブリッシュするイベントの型。EventStreamではAnyRefになっているが、もっと限定することも可能。
* Subscriber  
  イベントに登録サブスクライバーの型。EventStreamではActorRef。
* Classifier  
  イベントを送信するときのサブスクライバを選択に使用する分類子。EventStreamではメッセージのクラス型。
  
また、これらを全て実装しなくても `LookupClassification, SubchannellClassification, ScanningClassification` などのもっと用途に近い抽象クラスがあるため、それらをミックスインすると実装が楽になる。

## DeadLetterChannel
失敗したメッセージが配信されるチャネル。 `DeadLetter` クラスをサブスクライブすることによって、配信に失敗したメッセージを取得することができる。  
処理されていないメッセージがわかるのでシステムの不具合を調べるのに役立つ。  
また、 `system.deadLetters` に直接メッセージを送信することも可能。

## 補償配信チャネル
メッセージが受信者に配信されることを保証するPoint to Point Channel。  
ReliableProxyを使用すると、リモートアクターに高い保証が得られる。ただし、ローカルでのイベント配信については基本的に失敗することはないので、特に特別な配信チャネルは用意されていない。  
またjvmのエラーやクリティカルなエラーでは、(他のシステム同様に)メッセージの送信が失敗したり、破棄されてしまうことはある。



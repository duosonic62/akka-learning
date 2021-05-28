# アクターのテスト
Akkaのテストを行う際は `akk-testkit` を使うことで比較的容易にテストが実行できる。

## TestProbe
* アクターからの返信を受信できる
* メッセージを検査できる
* 特定のメッセージが到着するタイミングを設定できる

## TestKit
* メッセージの期待値をアサートする

## テストのサポートtrait
テストの準備と後処理は結構面倒なので、サポート用のtraitを用意しておいてすると良い。  
詳細はchapter2 の StopSystemAfterAll を確認。

## SilentActorのテスト
SilentActorとは、外部から振る舞いが確認できないアクターのこと。  
下記の2パターンの場合が存在する。  

* 内部に状態を持っており、メッセージの受信でその内部の状態を変更するアクター
* 次のアクターにメッセージを送信するための中間ステップ的なアクター

内部に状態を持つ場合は `TestActorRef` でアクターを作成して、メッセージの送信後 `underlyingActor` で内部アクターを取得し状態を確認する。  

```scala
val silentActor = TestActorRef[SilentActor]
                
silentActor ! SilentMessage("whisper")
silentActor.underlyingActor.state must contain("whisper")
```

中間的なアクターの場合はTestKitに含まれる `testActor` を送り先のアクターとして渡し、 `expectMesssage` で期待したメッセージが送られているか確認する。

```scala
val silentActor = system.actorOf(Props[SilentActor], "s3")
silentActor ! SilentMessage("whisper 1")
silentActor ! SilentMessage("whisper 2")

silentActor ! GetState(testActor) // testKitに含まれているtestActorにメッセージを送らせちゃう
expectMsg(Vector("whisper 1", "whisper 2"))
```

## SendingActorのテスト
SengdingActorとは、アクターが受信したメッセージの処理を行った後に次のアクターにメッセージを受け渡すアクターのこと。

Propsとして `testActor` を生成してテスト対象のアクターを生成する。

```scala
val props = SendingActor.props(testActor)
val sendingActor = system.actorOf(props, "sendingActor")
```

検証はexpectMsgPFを通して testActor に渡されたメッセージを確認する。

```scala
expectMsgPF() {
    case SortedEvents(events) => 
        events.size must be(size)
        unsorted.sortBy(_.id) must be(events)
}
```

## SideEffectingActor
SideEffectingActorとは別のアクターではないオブジェクトに影響を与えるアクターのこと。
例えばコンソールへの出力やDBへの値の保存など。

テスト用にアクターシステムを作成して、イベントハンドラをTestEventListnerにする。

```scala
val config = ConfigFactory.parseString(
            """
            akka.loggers = [akka.testkit.TestEventListener]
            """
        )
        ActorSystem("testsystem", config)
```

テストはEventFilterを使ってインターセプトして期待する値が取得できるかを確認する。

```scala
EventFilter.info(message = "Hello World!", occurrences = 1).intercept {
    actor ! Greeting("World")
} 
```

基本的にテストがやりにくい性質のため、 テスト対象のアクターに `Option` でテスト用のリスナーを受け取れるようにしておくのも良い。もしかしたら双方向にしちゃえばもっと良いかもしれない。

```scala
def receive = {
    case Greeting(who) => 
        val message = s"Hello $who!"
        log.info(message)
        listner.foreach(_ ! message)
}
```

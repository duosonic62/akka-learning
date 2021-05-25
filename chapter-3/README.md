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
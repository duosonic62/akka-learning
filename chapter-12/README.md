# Streaming
akka-streamでは有限のバッファー で無制限のストリームを処理する方法を提供する。  

1. プロデューサーが処理する要素を生成する
1. 処理ノードがプロデューサーが提供した要素を一つづつ処理する
1. コンシューマーに処理の完了した要素を渡す

これら一連の処理をまとめて `グラフ` と呼ぶ。

## akka-stream(Source & Sync)
akka-streamを使用するには下記の手順が必要となる。  

1. 処理フロー(グラフ)を定義
1. 処理フローを実行(マテリアライズ)

ここでは2つのエンドポイント(ノード)だけを使用した簡単な例をしめす。

### 処理フロー(グラフ)の定義
処理フローを定義するためには `エンドポイント(ノード)` を生成してそれらをつなぎ合わせる必要がある。  
簡単な例では `1つの出力を持つSource` と、 `1つの入力を持つSink` を接続することによって `RunnableGraph` を生成する。  
ちなみにグラフ単体では実行可能ではなく、暗黙的に宣言されている `マテリアライザー` がアクターを生成して、実行可能な状態になる。

```scala
val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputPath)
val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)
```

生成したグラフはそのままでは特に何も動作しないため、実行する必要がある。

### 処理フローを実行(マテリアライズ)
生成したグラフは `run()` メソッドで実行することができる。実行結果として `補助値(マテリアライズされた値)` を受け取る。  
補助値はグラフを生成するときにどのエンドポイントの補助値を受け取る(受け取らない)を設定することができる。

```scala
  // ActorMateliarizerで実行するときはActorSystemが必要
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  // materilalizerは systemから暗黙的に作成されるようになったので下記は不要
  // implicit val matelialier = ActorMatelializer()

  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
```

## akka-stream(Flow)
上記の例では入力と出力のノードのみだったが、 `Flowは入力と出力を一つづつ持つ` ため3以上のノードを定義できるようになる。  
例えば上記の例に加えフィルターやパース、シリアライズを行うノードを追加して高機能なstreamを生成することができる。

```scala
TODO
```

### エラーハンドリング
エラーハンドリングはアクターと同様にスーパーバイザー戦略を適用することができる。  
```scala
  val decider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }
  val parse: Flow[String, Event, NotUsed] = Flow[String].map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
```

またエラーをストリーム要素とし手流してしまうのもの手段の一つとして取れる。
```scala
val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map { evt =>
      val parsedEvt = try {
        LogStreamProcessor.parseLineEx(evt)
      } catch {
        case e: LogParseException => Failure(e)
      }
      Success(parsedEvt)
    }
```

## akka-stream(BidiFlow)
BidiFlowは2入力、2出力を持つコンポーネント。シリアライザ・デシリアライザをひとまとめにしてプロトコルアダプターなどとして活用する際に便利になる。  
基本的には2つのFlowをひとまとめにしたようなものなので、BidiFlowをFlowとつなげた場合は下記のように処理される。  
input -> BidiFlow(in: 1) -> BidiFlow(out: 1) -> Flow(in) -> Flow(out) -> BidiFlow(in:2) -> BidiFlow(out: 2) -> output

```scala
val bidiFlow = BidiFlow.fromFlows(inFlow, outFlow)
val composedFlow = bidiFlow.join(filter)
```

## Graph DSL
akka-streamでは任意の数の入出力を持つファンイン・ファンアウトのシナリオを記述できるグラフDSLを提供している。  
akka.stream.Shapeを継承したクラスでGraphを作れれば、柔軟な形を持ったstreamのコンポーネントを自作できる。

例えば、1in/1outのグラフであれば、FlowShape(extends Shape)を使えるので下記のように書ける。  
```scala
 Flow.fromGraph(
      GraphDSL.create() { implicit builder => 
      ・・・
　}
```

またこれ自体がグラフなのでShapeで定義してある入出力数を守れば、内部的には自由なグラフを作れる。
```scala
broadcast ~> parserWrapped.in
broadcast ~> ok ~> decode ~> logFileSink(Ok)
broadcast ~> warning ~> decode ~> logFileSink(Warning)
broadcast ~> error ~>  decode ~> logFileSink(Error)
broadcast ~> critical ~> decode ~> logFileSink(Critical)

FlowShape(broadcast.in, parserWrapped.out)
```

broadcastはbuilderに登録しているinの部分、parserWrappedはbuilderに追加しているoutの部分である。  
最後にFlowShapeを作るときに、 builderに登録してあるin/out を使う必要があるので注意。  
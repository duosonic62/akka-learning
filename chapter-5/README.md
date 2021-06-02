# Future
Futureは `関数を非同期で合成する` ツール。
アクターが状態を保持したり回復性を持つ息の長い `オブジェクト` に対して、 Futureは状態を保持しない場合の `関数` に適用すると効果的である。 

## Futureのユースケース
* ノンブロッキング
* 関数を一度だけ呼び出し、将来のある時点で `結果` を使う
* 結果を合成したい
* 同じ機能を持った複数の関数を呼び出し、一番早かった結果などを使う
* 特定の関数が、他の関数の結果に依存するような関数をパイプライン化する
 
また、Akkaで `ask` メソッドを使用すると、戻り値が `Future` となる。

## 使いかた
下記のような呼び出しをすると結果の型が `Future[TicketActor.TicketInfo]` になる。

```scala
ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
```

`map` を使って値を特定の型に変換したり、 `forEach` で副作用を発生させても良い。 

```scala
ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
  .map {
    _.toString // Stringに変換
  }

ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
  .forEach(println) // 値を出力
```

複数のFutureを合成させるのであれば、 `for式` を用いると便利。下記の場合だと、ticket, userの両方の計算が完了した時にyield以下のコードが走る。  
infoに入る値は Future[UserInfo] になる。

```scala
  val info = for {
    ticket <- ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
    user <- userActor.ask(UserActor.GetUser(3)).mapTo[UserActor.User]
  } yield UserTicket(
    userId = user.id,
    userName = user.name,
    ticketId = ticket.id,
    ticketName = ticket.name
  )
```

また、例外をハンドリングする場合は onComplete か recover を使用する。 

副作用がある場合はonCompleteを使用する。
```scala
ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
  .onComplete {
    case Success(value) => value
  }
```

返り値が欲しい場合は、 recoverで返り値を設定する。 

```scala
  val ticket: Future[Option[TicketActor.TicketInfo]] = ticketActor.ask(TicketActor.GetTicketInfo(1)).mapTo[TicketActor.TicketInfo]
    .map(Some(_))
    .recover { case e: Exception => {
      println(e)
      None
    }
    }
```


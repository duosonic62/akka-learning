#  Actor Persistence
akkaでは akka-persistence を用いることにより、アクターの状態を永続化することができる。  
akka-persistenceでは一般的なイミュータブルなDBへの永続化の方法ではなく `イベントソーシング` による状態の永続化を行う。  

## イベントソーシング
イベントソーシングでは、成功した全ての操作を `イベント` として `ジャーナル` に保存する。  
例えば電卓のアプリケーションがあった場合にイベントソーシングで記録する情報は、計算の操作一つ一つをジャーナルに登録する。  
一般的なDBのCRUD操作ではその時々の最後の計算結果を保持しているのに対し、イベントソーシングでは中間の計算結果もイベントを復元することで再現できる。  
また基本的に保存するイベントはイミュータブルなため、並行アクセスの制御はしやすい。

## akka-persistence
akka-persistenceではイベントソーシングを用いてアクターの状態の永続化をおこなう。ジャーナルにはDBだけでなく、NoSQLや様々な永続化先の選択肢がある。   
 
### スナップショット
アクターの回復時にはイベントを全て読み込む必要があるため、完了するまでにかなりの時間をようすることがある。  
そのようなことが起こり得る場合は `スナップショット` を使って、ある時点でのアクターの状態を保存しておき、回復までの時間を早めることができる。

アクターのスナップショットをとるのは任意のタイミングで実行可能で、下記のようにsaveSnapshotを呼び出す。  
おそらく性能的な用件があるため、毎回取らなくても良いかもしれない。  

```scala
saveSnapshot(Basket.Snapshot(items))
```

アクターの状態を回復するときは `receiveRecover` が呼ばれる。snapshotは下記のように `SnapshotOffer` のcase classにラップされてくる。

```scala
  override def receiveRecover: Receive = {
    case event: Event => // スナップショット後に発行されたイベントはそのまま渡される
      nrEventsRecovered = nrEventsRecovered + 1
      updateState(event)
    case SnapshotOffer(_, snapshot: Basket.Snapshot) => // スナップショットはSnapshotOffer クラスにラップされる
      log.info(s"Recovering baskets from snapshot: $snapshot for $persistenceId")
      items = snapshot.items
  }
```
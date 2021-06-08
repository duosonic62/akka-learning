# Akkaでの分散アプリケーション
Akkaでは `akka-remote` モジュールを用いることでネットワークを介してアクターどうしで通信を行うことができる。

## akka-remoteモジュールを用いた分散アプリケーション
Akkaではリモートのアクターへの参照を下記二つの方法で実現する。

1. パスによってアクターを検索する `リモート参照`
1. 生成したアクターの参照をリモートにデプロイする `リモートデプロイ` 

### リモート参照 
リモート参照では直接アクターの `パス` を指定して通信を行う。

```scala
  val path = "akka://simple-backend@127.0.0.1:2551/user/simple-backend"
  val backendActor = frontend.actorSelection(path)
```

## リモートデプロイ
リモートデプロイではアクターを使用する側が、使用するアクターをリモートにデプロイして通信を行う。

使用される側のアプリケーションは、ActorSystemを構築するだけ。

```scala
object BackendApp extends App {
  val config = ConfigFactory.load("backend")
  implicit val system: ActorSystem = ActorSystem(BackendActor.name, config)
}
```

使用する側のアプリケーションはリモート環境にアクターをデプロイして使用する。

```scala
object FrontendApp extends App {
  val config = ConfigFactory.load("frontend")
  implicit val system: ActorSystem = ActorSystem("frontend", config)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val backendActor: ActorRef = system.actorOf(Props[BackendActor], BackendActor.name)
}
```


name := "goticks"

version := "1.0"

organization := "com.example"

scalaVersion := "2.12.13"

libraryDependencies ++= {
  val akkaVersion = "2.6.14"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"     % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
    "org.scalatest"     %% "scalatest"       % "3.2.9"       % "test"
  )
}
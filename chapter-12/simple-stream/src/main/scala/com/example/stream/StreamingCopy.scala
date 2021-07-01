package com.example.stream

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, RunnableGraph, Sink, Source}
import akka.util.ByteString

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._
import scala.concurrent.{ExecutionContextExecutor, Future}

object StreamingCopy extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val inputPath = Paths.get("sample.txt")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputPath)

  val outputFile = Paths.get("sample_copy.txt")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)
  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
}

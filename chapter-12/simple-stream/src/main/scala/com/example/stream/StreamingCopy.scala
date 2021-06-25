package com.example.stream

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink, Source, RunnableGraph}
import akka.util.ByteString

import java.nio.file.{Path, Paths}
import java.nio.file.StandardOpenOption._
import scala.concurrent.Future

object StreamingCopy extends App {
  val inputPath = Paths.get("")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputPath)

  val outputFile = Paths.get("")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)
}

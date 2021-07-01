package com.example.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Keep}
import akka.stream.{FlowShape, Graph}
import akka.util.ByteString
import com.example.stream.StreamingFilter.{inputPath, maxLine, outputFile, system}

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import scala.concurrent.ExecutionContextExecutor

object BroadcastStreamingFilter  extends App with EventMarshalling {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  type FlowLike = Graph[FlowShape[Event, ByteString], NotUsed]

  logFileSource.via(json).via(processStates).toMat(logFileSink())(Keep.right)
    .run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }

  def processStates: Graph[FlowShape[Event, ByteString], NotUsed] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._
        val bcast = builder.add(Broadcast[Event](5))
        val out = builder.add(Flow[Event].map { event =>
          ByteString(event.toString + "\n")
        })

        val ok = Flow[Event].filter(_.state == Ok)
        val warning = Flow[Event].filter(_.state == Warning)
        val error = Flow[Event].filter(_.state == Error)
        val critical = Flow[Event].filter(_.state == Critical)

        bcast ~> out.in
        bcast ~> ok ~> out ~> logFileSink(Ok)
        bcast ~> warning ~> out ~> logFileSink(Warning)
        bcast ~> error ~>  out ~> logFileSink(Error)
        bcast ~> critical ~> out ~> logFileSink(Critical)

        FlowShape(bcast.in, out.out)
      }
    )
  }

  def logFileSource = {
    val inputPath = Paths.get("sample.log")
    FileIO.fromPath(inputPath)
  }

  def json: Flow[ByteString, Event, NotUsed] = Framing.delimiter(ByteString("\n"), maxLine)
    .map { str => str.decodeString("UTF8") }
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }

  def logFileSink(state: State) = {
    val outputFile = Paths.get(s"sample_$state.log")
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))
  }

  def logFileSink() = {
    val outputFile = Paths.get(s"sample_copied.log")
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))
  }
}

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

  logFileSource.via(jsonParser).via(processStates).toMat(logFileSink())(Keep.right)
    .run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }

  def processStates: Graph[FlowShape[Event, ByteString], NotUsed] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._
        val broadcast = builder.add(Broadcast[Event](5))
        val parserWrapped = builder.add(decode)

        val ok = Flow[Event].filter(_.state == Ok)
        val warning = Flow[Event].filter(_.state == Warning)
        val error = Flow[Event].filter(_.state == Error)
        val critical = Flow[Event].filter(_.state == Critical)

        broadcast ~> parserWrapped.in
        broadcast ~> ok ~> decode ~> logFileSink(Ok)
        broadcast ~> warning ~> decode ~> logFileSink(Warning)
        broadcast ~> error ~>  decode ~> logFileSink(Error)
        broadcast ~> critical ~> decode ~> logFileSink(Critical)

        FlowShape(broadcast.in, parserWrapped.out)
      }
    )
  }

  def logFileSource = {
    val inputPath = Paths.get("sample.log")
    FileIO.fromPath(inputPath)
  }

  def jsonParser: Flow[ByteString, Event, NotUsed] = Framing.delimiter(ByteString("\n"), 512)
    .map { str =>str.decodeString("UTF8")}
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }

  def decode = Flow[Event].map { event =>
    ByteString(event.toString + "\n")
  }

  def logFileSink(state: State) = {
    val outputFile = Paths.get(s"sample_$state.log")
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))
  }

  def logFileSink() = {
    val outputFile = Paths.get(s"sample_copied.log")
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))
  }

}

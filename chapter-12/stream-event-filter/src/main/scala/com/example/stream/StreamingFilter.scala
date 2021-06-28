package com.example.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.FSM.Failure
import akka.actor.TypedActor.Supervisor
import akka.stream.{ActorAttributes, IOResult, Supervision}
import akka.util.ByteString

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._
import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.stream.scaladsl._
import com.example.stream.LogStreamProcessor.LogParseException

import scala.util.Success


object StreamingFilter extends App with EventMarshalling {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val inputPath = Paths.get("sample.log")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputPath)

  val outputFile = Paths.get("sample_filtered.log")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val maxLine = 512

  val frame: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), maxLine)
    .map { str => str.decodeString("UTF8") }

  val decider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }
  val parse: Flow[String, Event, NotUsed] = Flow[String].map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
    .withAttributes(ActorAttributes.supervisionStrategy(decider))

  val filter: Flow[Event, Event, NotUsed] = Flow[Event].filter(_.state == Ok)

  val serialize: Flow[Event, ByteString, NotUsed] = Flow[Event].map { event =>
    ByteString(event.toString + "\n")
  }

  val composedFlow: Flow[ByteString, ByteString, NotUsed] =
    frame.via(parse)
      .via(filter)
      .via(serialize)

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(composedFlow).toMat(sink)(Keep.right)

  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
}

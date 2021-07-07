package com.example.persistent

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.Config
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.File
import scala.collection.immutable
import scala.util.Try

abstract class PersistenceSpec(system: ActorSystem) extends TestKit(system)
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with PersistenceCleanup {

  def this(name: String, config: Config) = this(ActorSystem(name, config))

  override protected def beforeAll(): Unit = deleteStorageLocations()

  override protected def afterAll(): Unit = {
    deleteStorageLocations()
    TestKit.shutdownActorSystem(system)
  }

  def killActors(actors: ActorRef*): Unit = {
    actors.foreach { actor =>
      watch(actor)
      system.stop(actor)
      expectTerminated(actor)
      Thread.sleep(1000)
    }
  }
}


trait PersistenceCleanup {
  def system: ActorSystem

  val storageLocations: immutable.Seq[File] = List(
    "akka.persistence.journal.leveldb.dir",
    "akka.persistence.journal.leveldb-shared.store.dir",
    "akka.persistence.snapshot-store.local.dir").map { s =>
    new File(system.settings.config.getString(s))
  }

  def deleteStorageLocations(): Unit = {
    println(storageLocations)
    storageLocations.foreach(dir => Try(FileUtils.deleteDirectory(dir)))
  }
}
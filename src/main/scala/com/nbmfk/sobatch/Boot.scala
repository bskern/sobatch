package com.nbmfk.sobatch

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.nbmfk.sobatch.actors.DataStore.QueryMapSize
import com.nbmfk.sobatch.actors.{DataFetcher, DataStore}
import com.nbmfk.sobatch.service.{DefaultEmailService, DefaultQuestionService}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.io.StdIn

object Boot extends {
  def main(args: Array[String]) {
    println("Starting up")
    implicit val system = ActorSystem("sobatch")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()
    val logger = Logging(system, getClass)

    import DataFetcher._

    val config = ConfigFactory.load()
    val questService = new DefaultQuestionService(config)
    val emailService = new DefaultEmailService()

    val dataStore = system.actorOf(Props(new DataStore(emailService)), name = "dataStore")
    val dataFetcher = system.actorOf(Props(new DataFetcher(questService, dataStore)), name = "dataFetcher")


    val cancellable =
      system.scheduler.schedule(
        0 milliseconds,
        15 minutes,
        dataFetcher,
        GetData)

    val cancellableMapSize =
      system.scheduler.schedule(
        1 minutes,
        30 minutes,
        dataStore,
        QueryMapSize)

    println(s"Server online Press RETURN to stop...")
    StdIn.readLine() //run until user presses return
    cancellable.cancel()
    cancellableMapSize.cancel()
    system.terminate()
  }
}


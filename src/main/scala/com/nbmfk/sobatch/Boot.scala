package com.nbmfk.sobatch

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.nbmfk.sobatch.actors.{DataFetcher, DataStore}
import com.nbmfk.sobatch.service.{DefaultEmailService, DefaultQuestionService}
import com.typesafe.config.ConfigFactory
import net.sf.ehcache.{Cache, CacheManager}
import net.sf.ehcache.config.PersistenceConfiguration.Strategy
import net.sf.ehcache.config.{CacheConfiguration, PersistenceConfiguration}
import net.sf.ehcache.store.MemoryStoreEvictionPolicy

import scala.concurrent.duration._
import scala.io.StdIn
import scalacache.ehcache.EhcacheCache

object Boot  {
  def main(args: Array[String]) {
    println("Starting up")
    implicit val system = ActorSystem("sobatch")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()


    import DataFetcher._


    val config = ConfigFactory.load()
    val questService = new DefaultQuestionService(config)
    val emailService = new DefaultEmailService(config)

    val cacheMgr = CacheManager.create()
    val myCache = new Cache(
       new CacheConfiguration("questionIds", 100)
          .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
            .eternal(false)
        .timeToLiveSeconds(86400)
        .timeToIdleSeconds(43200)
        .diskExpiryThreadIntervalSeconds(0)
          .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)))
      cacheMgr.addCache(myCache)

    import scalacache._
    implicit val scalaCache = ScalaCache(EhcacheCache(myCache))


    val dataStore = system.actorOf(Props(new DataStore(emailService)), name = "dataStore")
    val dataFetcher = system.actorOf(Props(new DataFetcher(questService, dataStore)), name = "dataFetcher")


    val cancellable =
      system.scheduler.schedule(
        0 milliseconds,
        15 minutes,
        dataFetcher,
        GetData)

    println(s"Server online Press RETURN to stop...")
    StdIn.readLine() //run until user presses return
    cancellable.cancel()
    system.terminate()
  }
}


package com.nbmfk.sobatch.actors

import akka.actor.{Actor, ActorLogging}
import com.nbmfk.sobatch.actors.DataStore.NewQuestion
import com.nbmfk.sobatch.model.SOQuestion
import com.nbmfk.sobatch.service.EmailService

import scalacache.ScalaCache
import scalacache.serialization.InMemoryRepr

object DataStore {
  case class NewQuestion(q:SOQuestion)
}
class DataStore(emailService: EmailService)(implicit questions: ScalaCache[InMemoryRepr]) extends Actor with ActorLogging {


  import scalacache._

  override def receive: Receive =  {
    case NewQuestion(quest) =>
      log.info("Received New Question message")
      sync.get(quest.question_id) match {
        case None =>
          log.info("was new question ..sending email")
          emailService.sendEmail(quest)
          put(quest.question_id)(quest)
        case Some(q) => log.debug(s"existing question id ${quest.question_id} doing nothing")
      }
    case _ => //do nothing
  }
}

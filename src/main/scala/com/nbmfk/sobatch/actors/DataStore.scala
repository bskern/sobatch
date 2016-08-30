package com.nbmfk.sobatch.actors

import akka.actor.{Actor, ActorLogging}
import com.nbmfk.sobatch.actors.DataStore.{NewQuestion, QueryMapSize}
import com.nbmfk.sobatch.model.SOQuestion
import com.nbmfk.sobatch.service.EmailService

object DataStore {
  case class NewQuestion(q:SOQuestion)
  case object QueryMapSize
}
class DataStore(emailService: EmailService) extends Actor with ActorLogging {


  val questions = collection.mutable.Map[Long,SOQuestion]()


  override def receive: Receive =  {

    case NewQuestion(quest) =>
      log.info("Received New Question message")
      if(!questions.keySet.contains(quest.question_id)) {
        log.info("was new question ..sending email")
        emailService.sendEmail(quest)
      }
      questions.put(quest.question_id,quest)
    case QueryMapSize => log.info(s"*** map contains ${questions.size} elements***")
    case _ => //do nothing
  }
}

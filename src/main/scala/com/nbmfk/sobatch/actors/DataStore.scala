package com.nbmfk.sobatch.actors

import akka.actor.Actor

import com.nbmfk.sobatch.actors.DataStore.{NewQuestion, QueryMapSize}
import com.nbmfk.sobatch.model.SOQuestion
import com.nbmfk.sobatch.service.EmailService

object DataStore {
  case class NewQuestion(q:SOQuestion)
  case object QueryMapSize
}
class DataStore(emailService: EmailService) extends Actor {


  val questions = collection.mutable.Map[Long,SOQuestion]()


  override def receive: Receive =  {

    case NewQuestion(quest) =>
      if(!questions.keySet.contains(quest.question_id)) {
        emailService.sendEmail(quest)
      }
      questions.put(quest.question_id,quest)
    case QueryMapSize => println(s"*** map contains ${questions.size} elements***")
    case _ => //do nothing
  }
}

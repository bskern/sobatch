package com.nbmfk.sobatch.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.nbmfk.sobatch.actors.DataFetcher.{GetData, GotData}
import com.nbmfk.sobatch.service.{EmailService, QuestionService}
import com.nbmfk.sobatch.model.SOQuestion
import org.scalactic.{Bad, Good, Or}

object DataFetcher {
  case object GetData
  case class GotData(result:Seq[SOQuestion] Or String)
}

class DataFetcher(questionService: QuestionService,dataStore: ActorRef) extends Actor with ActorLogging {
 import context.dispatcher

  import DataStore._


  def receive = {
    case GetData =>
      log.info("received GetData message")
      questionService.getQuestions().onComplete { f => self ! GotData(result=f.get)}
    case GotData(res) => res match {
      case Good(questions) =>  log.info("successfully got questions about to send them to data store");questions.foreach(dataStore ! NewQuestion(_))
      case Bad(err) => log.error(s"error $err")
    }
    case _ => //do nothing


  }

}
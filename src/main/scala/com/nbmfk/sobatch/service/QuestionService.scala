package com.nbmfk.sobatch.service

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.nbmfk.sobatch.model.{QuestionList, RawQuestion, SOQuestion}
import com.typesafe.config.Config
import akka.http.scaladsl.model.StatusCodes._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import org.scalactic._
import spray.json.DefaultJsonProtocol



trait QuestionService {
    def getQuestions():Future[Seq[SOQuestion] Or String]
}

class DefaultQuestionService(config: Config)(implicit ec:ExecutionContext,system:ActorSystem,mat:Materializer) extends QuestionService with
  DefaultJsonProtocol
   with SprayJsonSupport with LazyLogging {

  implicit val SOQuestionFormat = jsonFormat5(SOQuestion)
  implicit val RawQuestionFormat = jsonFormat5(RawQuestion)
  implicit val QuestionListFormat = jsonFormat1(QuestionList)

  override def getQuestions(): Future[Seq[SOQuestion] Or String]= {
     logger.info("fetching questions")
      fetchData().flatMap { response =>
        response.status match {
          case OK => logger.info("got 200 OK response from SO");parseTransformJSON(response).map(Good(_))
          case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
            val error = s"something went wrong status code ${response.status} and entity $entity"
            Future.successful(Bad(error))
          }
        }
      }
  }


lazy val soApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
  Http().outgoingConnectionHttps(config.getString("stackoverflow.apiAddress"))


  def parseTransformJSON(resp: HttpResponse):Future[Seq[SOQuestion]] = {
    for {
      byteString <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      decompressedBytes <- Gzip.decode(byteString)
      questionList <- Unmarshal(decompressedBytes).to[QuestionList]
      result <- Future.successful(transformQuestions(questionList))
    } yield result
  }

  private def transformQuestions(raw:QuestionList): Seq[SOQuestion] = {
    raw.items.map(rawToSOQuestion)
  }

  private def rawToSOQuestion(q:RawQuestion):SOQuestion = {
    def niceDateString(input:Long):String = {
      java.util.Date.from(Instant.ofEpochSecond(input)).toString
    }

    val created = niceDateString(q.creation_date)
    val updated = niceDateString(q.last_activity_date)
    SOQuestion(q.title,q.question_id,created,updated,q.link)
  }

  private def fetchData():Future[HttpResponse] = {
    Source.single(buildHttpRequest()).via(soApiConnectionFlow).runWith(Sink.head)
  }

  private def buildHttpRequest():HttpRequest = {
    HttpRequest(uri = Uri(config.getString("stackoverflow.uri")).withQuery(Query(buildParams())))
  }


  private def buildParams():Map[String,String] = {
    Map(
      "key" -> config.getString("stackoverflow.apiKey"),
      "pagesize" -> config.getString("stackoverflow.pageSize"),
      "tagged" -> "scala",
      "sort" -> "activity",
      "order" -> "desc",
      "site" -> "stackoverflow")
  }
}


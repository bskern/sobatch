package com.nbmfk.sobatch.service

import com.nbmfk.sobatch.model.SOQuestion
import javax.mail._
import javax.mail.internet._
import java.util.Date
import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}


trait EmailService {
  def sendEmail(question:SOQuestion):Unit
}

class DefaultEmailService(config: Config) extends EmailService with LazyLogging {
  override def sendEmail(question: SOQuestion): Unit = {
    logger.info(s"going to send email for question $question")
    val agent = new MailAgent(to=config.getString("emailNotifications.to"),
      cc="",
      bcc="",
      from=config.getString("emailNotifications.from"),
      subject=s"new scala so: ${question.title}",
      content=s"${question.title} link here ${question.link}",
      smtpHost = config.getString("emailNotifications.smtpHost"),
      smtpPort = config.getString("emailNotifications.smtpPort"))

      Try(agent.sendMessage) match {
        case Success(v) => logger.info(s"sent email..")
        case Failure(e) => logger.error(s"error sending message..error $e msg $agent")
      }
     
  }
}



class MailAgent(to: String,
                cc: String,
                bcc: String,
                from: String,
                subject: String,
                content: String,
                smtpHost: String,
                smtpPort: String)
{
  var message: Message = null

  message = createMessage
  message.setFrom(new InternetAddress(from))
  setToCcBccRecipients

  message.setSentDate(new Date())
  message.setSubject(subject)
  message.setText(content)

  // throws MessagingException
  def sendMessage {
    Transport.send(message)
  }

  def createMessage: Message = {
    val properties = new Properties()
    properties.put("mail.smtp.host", smtpHost)
    properties.put("mail.smtp.port", smtpPort)
    val session = Session.getDefaultInstance(properties, null)
    return new MimeMessage(session)
  }

  // throws AddressException, MessagingException
  def setToCcBccRecipients {
    setMessageRecipients(to, Message.RecipientType.TO)
    if (cc != null) {
      setMessageRecipients(cc, Message.RecipientType.CC)
    }
    if (bcc != null) {
      setMessageRecipients(bcc, Message.RecipientType.BCC)
    }
  }

  // throws AddressException, MessagingException
  def setMessageRecipients(recipient: String, recipientType: Message.RecipientType) {
    // had to do the asInstanceOf[...] call here to make scala happy
    val addressArray = buildInternetAddressArray(recipient).asInstanceOf[Array[Address]]
    if ((addressArray != null) && (addressArray.length > 0))
    {
      message.setRecipients(recipientType, addressArray)
    }
  }

  // throws AddressException
  def buildInternetAddressArray(address: String): Array[InternetAddress] = {
    // could test for a null or blank String but I'm letting parse just throw an exception
    return InternetAddress.parse(address)
  }

  override def toString : String =
    s"to-$to from-$from subject $subject smtphost $smtpHost smtpport $smtpPort"

}
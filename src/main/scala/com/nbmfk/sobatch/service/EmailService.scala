package com.nbmfk.sobatch.service

import com.nbmfk.sobatch.model.SOQuestion
import javax.mail._
import javax.mail.internet._
import java.util.Date
import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions._


trait EmailService {
  def sendEmail(question:SOQuestion):Unit
}

class DefaultEmailService(config: Config) extends EmailService with LazyLogging {
  override def sendEmail(question: SOQuestion): Unit = {
    logger.info(s"going to send email for question $question")
    new MailAgent(to=config.getString("emailNotifications.to"),
      cc="",
      bcc="",
      from="emailNotifications.from",
      subject=s"new scala so: ${question.title}",
      content=s"${question.title} link here ${question.link}",
      smtpHost = config.getString("emailNotifications.smtpHost")).sendMessage
      logger.info(s"sent email..")
  }
}



class MailAgent(to: String,
                cc: String,
                bcc: String,
                from: String,
                subject: String,
                content: String,
                smtpHost: String)
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

}
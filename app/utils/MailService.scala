package utils

import javax.inject.Inject

import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer._

import scala.language.postfixOps
import scala.concurrent.duration._

trait MailService {
  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String = ""): Unit
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String = ""): Unit
}

class MailServiceImpl @Inject() (mailerClient: MailerClient) extends MailService  {

  def from: String = current.configuration.getString("play.mailer.from").getOrElse("UNKNOWN")

  def sendEmailAsync (recipients: String*)(subject: String, bodyHtml: String, bodyText: String = ""): Unit = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }
  def sendEmail (recipients: String*)(subject: String, bodyHtml: String, bodyText: String = ""): Unit = {
    mailerClient.send(Email(
      subject,
      from,
      recipients,

      // sends text, HTML or both...
      Some(bodyText),
      Some(bodyHtml)
    ))
  }
}

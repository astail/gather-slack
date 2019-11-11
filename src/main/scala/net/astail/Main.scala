package net.astail

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import slack.models.Message
import slack.rtm.SlackRtmClient

object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    logger.info("start app")

    val token = ConfigFactory.load.getString("gether_times_bot_user_oauth_access_token")

    implicit val system = ActorSystem("slack")
    implicit val ec = system.dispatcher

    val client = SlackRtmClient(token)

    client.onMessage { message =>
      val sendMessageOption: Option[ResponseMessage] = messageCheck(message, client)
      sendMessageOption.foreach {
        response =>
          logger.info("================================================================")
          logger.info(s"receiveMessage: ${message.text}")
          logger.info(s"sendMessage: ${response.text}")
          logger.info("================================================================")
          client.sendMessage(response.channel, response.text)
      }
    }
  }

  case class ResponseMessage(text: String, channel: String)

  case class libraryMessage(channel: String, userId: String)

  def messageCheck(message: Message, client: SlackRtmClient): Option[ResponseMessage] = {
    val botChannel = ConfigFactory.load.getString("gether_times_post_slack_channel")

    val channel = if (s"${botChannel.head}" == "#")
      client.state.getChannelIdForName(botChannel.tail).getOrElse("")
    else
      botChannel

    // 自分のIDを設定して自分以外は反応しないようにする
    val myUserId: String = "U054X0P0V"
    val libraryChannel: String = client.state.getChannelIdForName("library").getOrElse("")
    val lSetting = libraryMessage(libraryChannel, myUserId)

    if (lSetting == libraryMessage(message.channel, message.user)) {
      libraryMessageCheck(message, client, libraryChannel)
    } else if (message.channel != channel)
      if (message.channel != libraryChannel)
        message.text match {
          case "ping" => Some(ResponseMessage(s"<@${message.user}> pong", message.channel))
          case _ => Some(ResponseMessage(s"<#${message.channel}> / <@${message.user}>: ${message.text}", channel))
        }
      else None
    else None
  }


  def libraryMessageCheck(message: Message, client: SlackRtmClient, libraryChannel: String): Option[ResponseMessage] = {
    val rentalArg = if (s"${message.text.head}" == "a")
      "貸して"
    else if (s"${message.text.head}" == "b")
      "返す"

    // 2.13からtoIntOptionが使えるけどslack-scala-client 0.2.7の調子が悪いため2.13に対応してないバーションのまま
    def safeStringToInt(str: String): Option[Int] = {
      import scala.util.control.Exception._
      catching(classOf[NumberFormatException]) opt str.toInt
    }

    safeStringToInt(message.text.tail) match {
      case Some(_: Int) => Some(ResponseMessage(s"<@UKE4U4LDD> --${message.text.tail} $rentalArg", libraryChannel))
      case None => None
    }
  }
}

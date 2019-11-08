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

  def messageCheck(message: Message, client: SlackRtmClient): Option[ResponseMessage] = {
    val botChannel = ConfigFactory.load.getString("gether_times_post_slack_channel")
    val channel: String = client.state.getChannelIdForName(botChannel).getOrElse("")
    val userName: String = client.state.getUserById(message.user).flatMap(_.profile.flatMap(_.first_name)).getOrElse("")

    if (message.channel != channel)
      message.text match {
        case "ping" => Some(ResponseMessage(s"<@${message.user}> pong", message.channel))
        case _ => Some(ResponseMessage(s"${userName}: ${message.text}", channel))
      }
    else
      None
  }
}

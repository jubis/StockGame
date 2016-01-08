package me.matiass.actors.service

import akka.actor.Actor
import org.slf4j.LoggerFactory

class TransactionAuditor extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  def receive = {
    case BuyEvent(name) => logger.info(s"Buy completed in $name")
    case ex: Exception => logger.error(s"Transaction failed: ${ex.getMessage}")
  }
}

case class BuyEvent(portfolioName: String)
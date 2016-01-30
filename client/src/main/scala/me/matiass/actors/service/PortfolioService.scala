package me.matiass.actors.service

import akka.actor._
import akka.pattern.ask
import fi.hackathon.api._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PortfolioService {
  import me.matiass.actors.ActorBase._

  private val logger = LoggerFactory.getLogger(this.getClass)

  val bank = system.actorSelection(bankSystemBase + "bank")
  val auditor = system.actorOf(Props[TransactionAuditor], "auditor")

  private def getPortfolioActor(name: String) = system.actorSelection(bankSystemBase + "bank/" + name)

  def doBuy(subjectName: String, order: BuyOrder): Future[Portfolio] = {
    logger.info(s"Buy order to portfolio $subjectName. Buy ${order.number} x ${order.symbol}.")

    val buy = getPortfolioActor(subjectName) ? order
    buy
      .map { case portfolio: Portfolio => portfolio }
      .andThen {
        case Success(Portfolio(name, _, _)) => auditor ! BuyEvent(name)
        case Failure(_) => auditor ! new RuntimeException(s"Buy failed in portfolio $subjectName")
      }
  }
  def doBuy(subjectName: String, symbol: String, amount: Int): Future[Portfolio] = {
    doBuy(subjectName, BuyOrder(symbol, amount))
  }

  def createPortfolio(portfolioName: String) = {
    bank ! CreatePortfolio(portfolioName, 1000)
    doBuy(portfolioName, "MSFT", 1)
  }

  def getPortfolio(portfolioName: String): Future[PortfolioSnapshot] = {
    logger.info(s"Getting portfolio $portfolioName")

    (getPortfolioActor(portfolioName) ? ValueRequest())
      .map { case snapshot: PortfolioSnapshot => snapshot }
      .recover { case _ => PortfolioSnapshot("Test portfolio", 0, 0, List(AssetSnapshot(Asset("A", 1.1, 10), 1.0))) }
  }
}
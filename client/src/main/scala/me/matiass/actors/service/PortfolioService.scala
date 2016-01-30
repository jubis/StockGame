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

  def doBuy(subjectName: String, order: BuyOrder): Future[Boolean] = {
    logger.info(s"Buy order to portfolio $subjectName. Buy ${order.number} x ${order.symbol}.")

    doAction(subjectName, order, "Buy")
  }
  def doBuy(subjectName: String, symbol: String, amount: Int): Future[Boolean] = {
    doBuy(subjectName, BuyOrder(symbol, amount))
  }

  def doSell(subjectName: String, order: SellOrder): Future[Boolean] = {
    logger.info(s"Sell order to portfolio $subjectName. Sell all ${order.symbol}.")

    doAction(subjectName, order, "Sell")
  }

  def doAction(subjectName: String, msg: Any, actionType: String): Future[Boolean] = {

    val action = getPortfolioActor(subjectName) ? msg
    action
      .andThen {
        case Success(_) => auditor ! BuyEvent(subjectName)
        case Failure(_) => auditor ! new RuntimeException(s"$actionType failed in portfolio $subjectName")
      }
      .map(_ => true)
      .recover { case _ => false }
  }

  def createPortfolio(portfolioName: String) = {
    bank ! CreatePortfolio(portfolioName, 1000)
    doBuy(portfolioName, "MSFT", 1)
  }

  def getPortfolio(portfolioName: String): Future[PortfolioAnalysis] = {
    logger.info(s"Getting portfolio $portfolioName")

    (getPortfolioActor(portfolioName) ? ValueRequest())
      .map { case snapshot: PortfolioAnalysis => snapshot }
      .recover { case _ => PortfolioAnalysis(
                              "Test portfolio", 0, 100, 11, 1, 10,
                              List(AssetAnalysis(AssetSnapshot("A", 1.1, 10, 0L), 10, 1, 10))
                          )
      }
  }
}
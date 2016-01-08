package me.matiass.actors

import akka.actor._
import akka.pattern.ask
import fi.hackathon.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



object Main extends App {

  val api = new RestApi(new PortfolioService)

  /*while(!system.whenTerminated.isCompleted) {
    val line = readLine("Command> ")

    val newFolio = "new folio (\\w+)".r

    line match {
      case "exit" => system.terminate()
      case newFolio(name) => CreatePortfolio(name, 1000)
    }
  }*/
}

class PortfolioService {
  import ActorBase._

  val bank = system.actorSelection(bankSystemBase + "bank")
  val auditor = system.actorOf(Props[TransactionAuditor], "auditor")

  private def getPortfolioActor(name: String) = system.actorSelection(bankSystemBase + "bank/" + name)

  def doBuy(subjectName: String)(symbol: String, amount: Int) {
    val buy = getPortfolioActor(subjectName) ? BuyOrder(symbol, amount)
    buy
      .map { case portfolio: Portfolio => portfolio }
      .onSuccess { case Portfolio(name, _, _) => auditor ! BuyEvent(name) }
  }

  def createPortfolio(name: String) = {
    bank ! CreatePortfolio(name, 1000)
    doBuy(name)("MSFT", 1)
  }

  def getPortfolio(portfolioName: String): Future[PortfolioSnapshot] = {
    (getPortfolioActor(portfolioName) ? ValueRequest())
      .map { case snapshot: PortfolioSnapshot => snapshot }
      .recover { case _ => PortfolioSnapshot("Test portfolio", 0, 0, List()) }
  }
}

case class BuyEvent(portfolioName: String)

class TransactionAuditor extends Actor {
  def receive = {
    case BuyEvent(name) => println(s"Buy completed in $name")
  }
}
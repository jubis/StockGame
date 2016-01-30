package stockgame.bank

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Status}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import fi.hackathon.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class CompleteBuy(sender: ActorRef, price: CurrentPrice, count: Int)
case class CompleteSell(sender: ActorRef, price: CurrentPrice)

class PortfolioActor(market: ActorRef, analyzer: ActorRef, name: String, initialValue: BigDecimal) extends Actor {

  def waitForCommand(portfolio: Portfolio): Receive = {
    case msg: BuyOrder => {
      println(msg)

      val origSender = sender

      market.ask(GetCurrentPrice(msg.symbol.toUpperCase))(5 seconds)
        .map(price => CompleteBuy(origSender, price.asInstanceOf[CurrentPrice], msg.number))
        .pipeTo(self)
    }
    case msg: CompleteBuy => {

      println(msg)

      buy(portfolio, msg.price, msg.count) match {
        case Success(portfolioAfter) => {
          println("Buy success")
          msg.sender ! Success("Böö")
          context.become(waitForCommand(portfolioAfter))
        }
        case Failure(e) => {
          println(e.printStackTrace())
          msg.sender ! Status.Failure(e)
        }
      }
    }
    case msg: SellOrder => {
      println(msg)

      val origSender = sender

      market.ask(GetCurrentPrice(msg.symbol.toUpperCase))(5 seconds)
        .map(price => CompleteSell(origSender, price.asInstanceOf[CurrentPrice]))
        .pipeTo(self)
    }
    case msg: CompleteSell => {

      println(msg)

      val portfolioAfter = sell(portfolio, msg.price)

      println("Sell success")
      msg.sender ! Success("Böö")
      context.become(waitForCommand(portfolioAfter))
    }
    case msg: ValueRequest => {
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)

      val origSender = sender

      analyzer
        .ask(portfolio)(5 seconds)
        .map(analysis => {println(analysis); analysis})
        .pipeTo(origSender)
    }
  }

  private def buy(portfolio: Portfolio, price: CurrentPrice, count: Int): Try[Portfolio] = {
    val sum = price.ask * BigDecimal(count)

    if (sum <= portfolio.cash)
      Success(portfolio.copy(cash = portfolio.cash - sum, assets = portfolio.assets :+ Asset(price.symbol, price.ask, count, System.currentTimeMillis)))
    else
      Failure(new Exception("Insufficient cash"))
  }

  private def sell(portfolio: Portfolio, price: CurrentPrice): Portfolio = {
    val assets = portfolio.assets.filter(asset => asset.symbol == price.symbol)

    val assetValue = assets.map(_.count).sum * price.bid

    portfolio.copy(cash = portfolio.cash + assetValue, assets = portfolio.assets.filterNot(_.symbol == price.symbol))
  }

  override def receive = waitForCommand(Portfolio(name, initialValue, List()))
}
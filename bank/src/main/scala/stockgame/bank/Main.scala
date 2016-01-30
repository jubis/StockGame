package stockgame.bank

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import fi.hackathon.api._

import scala.concurrent.Await

object Main extends App {

  implicit val system = ActorSystem("BankSystem")


  val market = resolve(system.actorSelection("akka.tcp://MarketSystem@192.168.0.20:5551/user/market"))

  val bank = system.actorOf(Props.create(classOf[BankActor], market), name = "bank")

  def resolve(selection: ActorSelection): ActorRef = {
    val resolveTimeout = Timeout(5, TimeUnit.SECONDS)
    val awaitTimeout = Timeout(5, TimeUnit.SECONDS)

    Await.result(selection.resolveOne()(resolveTimeout), awaitTimeout.duration)
  }
}

class PortfolioActor(market: ActorRef, name: String, initialValue: BigDecimal) extends Actor {

  def waitForCommand(portfolio: Portfolio): Receive = {
    case msg: BuyOrder => {
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)
      val future = market ? GetCurrentPrice(msg.symbol)
      val currentPrice: Option[CurrentPrice] = Await.result(future, timeout.duration).asInstanceOf[Option[CurrentPrice]]

      val portfolioAfter = currentPrice match {
        case Some(price) => buy(portfolio, price, msg.number)
        case None => portfolio
      }

      sender ! portfolioAfter
      context.become(waitForCommand(portfolioAfter))
    }
    case msg: SellOrder => {
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)
      val future = market ? GetCurrentPrice(msg.symbol)
      val currentPrice: Option[CurrentPrice] = Await.result(future, timeout.duration).asInstanceOf[Option[CurrentPrice]]

      val portfolioAfter = currentPrice match {
        case Some(price) => sell(portfolio, price)
        case None => portfolio
      }

      sender ! portfolioAfter
      context.become(waitForCommand(portfolioAfter))
    }
    case msg: ValueRequest => {
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)
      val future = market ? portfolio
      val currentValue: PortfolioSnapshot = Await.result(future, timeout.duration).asInstanceOf[PortfolioSnapshot]

      sender ! currentValue
      context.become(waitForCommand(portfolio))
    }
  }

  private def buy(portfolio: Portfolio, price: CurrentPrice, count: Int) = {
    val sum = price.ask * BigDecimal(count)

    if(sum <= portfolio.cash)
      portfolio.copy(cash = portfolio.cash - sum, assets = portfolio.assets :+ Asset(price.symbol, price.ask, count, System.currentTimeMillis))
    else
      portfolio
  }

  private def sell(portfolio: Portfolio, price: CurrentPrice): Portfolio = {
    val assets = portfolio.assets.filter(asset => asset.symbol == price.symbol)

    val assetValue = assets.foldLeft(0) {(sum: Int, asset: Asset) => sum + asset.count} * price.bid

    portfolio.copy(cash = portfolio.cash + assetValue, assets = portfolio.assets.filterNot(_.symbol == price.symbol))
  }

  override def receive = waitForCommand(Portfolio(name, initialValue, List()))
}


class BankActor(market: ActorRef) extends Actor {

  override def receive = LoggingReceive {
    case msg: CreatePortfolio => {
      println("Create request")
      context.actorOf(Props.create(classOf[PortfolioActor], market, msg.name, msg.value), name = msg.name)
    }
    case _ => println("Something else")
  }
}

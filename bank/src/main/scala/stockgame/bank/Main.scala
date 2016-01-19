package stockgame.bank

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import fi.hackathon.api._

import scala.concurrent.Await

object Main extends App {

  implicit val system = ActorSystem("BankSystem")

  val market = system.actorSelection("akka.tcp://MarketSystem@192.168.0.16:8888/user/market")
  val bank = system.actorOf(Props.create(classOf[BankActor], market), name = "bank")

  bank ! CreatePortfolio("klasu", 1000.00)

  val portfolio = system.actorOf(Props.create(classOf[PortfolioActor], market, "klasu", BigDecimal(1000.00)), name = "klasu")

  portfolio ! BuyOrder("MSFT", 15)

  portfolio ! ValueRequest()
}

class PortfolioActor(market: ActorSelection, name: String, initialValue: BigDecimal) extends Actor {

  def waitForCommand(portfolio: Portfolio): Receive = {
    case msg: BuyOrder => {
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)
      val future = market ? GetCurrentPrice(msg.symbol)
      val currentPrice: Option[CurrentPrice] = Await.result(future, timeout.duration).asInstanceOf[Option[CurrentPrice]]

      val portfolioAfter = currentPrice match {
        case Some(price) => buy(portfolio, price, msg.number)
        case None => portfolio
      }

      println(portfolioAfter)

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


      println(currentValue)
      sender ! currentValue
      context.become(waitForCommand(portfolio))
    }
  }

  private def buy(portfolio: Portfolio, price: CurrentPrice, count: Int) = {
    val sum = price.ask * count
    portfolio.copy(cash = portfolio.cash - sum, assets = portfolio.assets :+ Asset(price.symbol, price.ask, count))
  }

  private def sell(portfolio: Portfolio, price: CurrentPrice) = {
    val asset = portfolio.assets.find(asset => asset.symbol == price.symbol)

    asset
      .map(as => portfolio.copy(cash = portfolio.cash + (as.count * price.bid), assets = portfolio.assets.filterNot(_.symbol == price.symbol)))
      .getOrElse(portfolio)
  }

  override def receive = waitForCommand(Portfolio(name, initialValue, List()))
}


class BankActor(market: ActorSelection) extends Actor {

  def managePortfolios(portfolios: List[ActorRef]): Receive = {
    case msg: CreatePortfolio => context.actorOf(Props.create(classOf[PortfolioActor], market, msg.name, msg.value), name = msg.name)
  }

  override def receive: Receive = managePortfolios(List())
}

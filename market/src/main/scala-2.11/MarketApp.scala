import akka.actor.{Actor, ActorSystem, Props}
import fi.hackathon.api._
import play.api.libs.json.Json

import scala.util.Try

object MarketApp extends App {
  implicit val system = ActorSystem("MarketSystem")
  val actor = system.actorOf(Props[MarketActor], name = "market")
}

class MarketActor extends Actor {

  override def receive: Receive = {
    case msg: GetCurrentPrice => sender ! getCurrentPrice(msg.symbol)
    case portfolio: Portfolio => sender ! calculatePortfolioValue(portfolio)
  }

  def yahooQuery(symbol: String): String = s"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%3D%22$symbol%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"

  def getCurrentPrice(symbol: String): Option[CurrentPrice] = {
    Try {
      val url = yahooQuery(symbol)
      val jsonStr = scala.io.Source.fromURL(url).mkString
      val json = Json.parse(jsonStr)
      val ask = (json \ "query" \ "results" \ "quote" \ "Ask").as[String]
      val bid = (json \ "query" \ "results" \ "quote" \ "Bid").as[String]
      Some(CurrentPrice(symbol, BigDecimal(ask), BigDecimal(bid)))
    }.getOrElse(None)
  }

  def calculatePortfolioValue(portfolio: Portfolio): PortfolioSnapshot = {
    val assets = portfolio.assets.map(asset => AssetSnapshot(asset, calculateAssetSymbolValue(asset)))
    PortfolioSnapshot(
      portfolio.name,
      portfolio.cash,
      portfolio.cash + assets.map(asset => asset.asset.count * asset.symbolValue).sum,
      assets
    )
  }

  def calculateAssetSymbolValue(asset: Asset): BigDecimal =
    getCurrentPrice(asset.symbol).map(_.bid).getOrElse(BigDecimal(0))

}
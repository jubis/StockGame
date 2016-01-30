package stockgame.bank

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import fi.hackathon.api._

import scala.concurrent.Await

class PortfolioAnalyzer(market: ActorRef) extends Actor {

  override def receive: Receive = {

    case portfolio: Portfolio => {
      val snapshot = calculatePortfolioValue(portfolio)

      val assetAnalyses = snapshot.assets.map(toAssetAnalysis)

      val buyValue = snapshot.assets.map(s => s.asset.buyPrice * s.asset.count).sum
      val totalValue = assetAnalyses.map(_.marketValue).sum
      val profit = assetAnalyses.map(_.profit).sum

      val profitPercentage = (profit / buyValue) * 100

      snapshot.copy(analysis = PortfolioAnalysis(totalValue, profit, profitPercentage, assetAnalyses))
    }
  }

  private def toAssetAnalysis(asset: AssetSnapshot) = {

    val marketValue = asset.symbolValue * asset.asset.count
    val buyValue = asset.asset.buyPrice * asset.asset.count

    val profit = marketValue - buyValue
    val profitPercentage = (profit / buyValue) * 100

    AssetAnalysis(asset, marketValue, profit, profitPercentage)
  }

  private def calculatePortfolioValue(portfolio: Portfolio): PortfolioSnapshot = {
    val assets = portfolio.assets.map(asset => AssetSnapshot(asset, calculateAssetSymbolValue(asset)))
    PortfolioSnapshot(
      portfolio.name,
      portfolio.cash,
      assets
    )
  }

  def calculateAssetSymbolValue(asset: Asset): BigDecimal = {
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    val priceFut = market ? GetCurrentPrice(asset.symbol)
    val price: Option[CurrentPrice] = Await.result(priceFut, timeout.duration).asInstanceOf[Option[CurrentPrice]]

    price.map(_.bid).getOrElse(0)
  }

}

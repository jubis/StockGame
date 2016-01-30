package stockgame.bank

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import fi.hackathon.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode

class PortfolioAnalyzer(market: ActorRef) extends Actor {

  override def receive: Receive = {

    case portfolio: Portfolio => {

      val assetSnapshots = portfolio.assets
        .map(fetchPrice)

      Future.sequence(assetSnapshots)
        .map(consolidate)
        .map(_.map(toAssetAnalysis))
        .map(analysePortfolio(portfolio, _))
        .pipeTo(sender)
    }
  }

  private def analysePortfolio(portfolio: Portfolio, assetAnalyses: List[AssetAnalysis]) = {

    val buyValue = assetAnalyses.map(s => s.asset.buyPrice * s.asset.count).sum.setScale(2, RoundingMode.CEILING)
    val marketValue = assetAnalyses.map(_.marketValue).sum.setScale(2, RoundingMode.CEILING)
    val profit = assetAnalyses.map(_.profit).sum.setScale(2, RoundingMode.CEILING)

    val profitPercentage = (if (buyValue != BigDecimal(0.00)) (profit / buyValue) * 100 else BigDecimal(0.00)).setScale(2, RoundingMode.CEILING)

    PortfolioAnalysis(portfolio.name, portfolio.cash, portfolio.cash + marketValue, marketValue,
                    profit, profitPercentage, portfolio.assets, assetAnalyses)
  }

  private def toAssetAnalysis(asset: AssetSnapshot) = {

    val marketValue = asset.symbolValue * asset.count
    val buyValue = asset.buyPrice * asset.count

    val profit = (marketValue - buyValue).setScale(2, RoundingMode.CEILING)
    val profitPercentage = ((profit / buyValue) * 100).setScale(2, RoundingMode.CEILING)

    AssetAnalysis(asset, marketValue, profit, profitPercentage)
  }

  private def consolidate(assets: List[AssetSnapshot]): List[AssetSnapshot] =
    assets.groupBy(_.symbol).values.map(avgAsset).toList

  private def avgAsset(assets: List[AssetSnapshot]): AssetSnapshot = {

    val totalValue = assets.foldLeft(BigDecimal(0.00)) { (sum: BigDecimal, asset) => sum + asset.buyPrice * asset.count }
    val count = assets.map(_.count).sum
    val avgPrice = totalValue / count

    AssetSnapshot(assets.head.symbol, avgPrice, count, assets.head.symbolValue)
  }

  private def fetchPrice(asset: Asset): Future[AssetSnapshot] = {

    market.ask(GetCurrentPrice(asset.symbol))(5 seconds)
      .map(price => AssetSnapshot(asset.symbol, asset.buyPrice, asset.count, price.asInstanceOf[CurrentPrice].bid))
  }

}

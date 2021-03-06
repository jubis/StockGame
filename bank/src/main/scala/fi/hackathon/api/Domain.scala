package fi.hackathon.api

import akka.actor.ActorRef


case class BuyOrder(symbol: String, number: Int)
case class SellOrder(symbol: String, number: Int)

case class CreatePortfolio(name: String, value: BigDecimal)
case class ListPortfolios()
case class PortfolioList(portfolios: List[ActorRef])

case class PortfolioStatusRequest()
case class ValueRequest()


case class AssetTransaction(symbol: String, buyPrice: BigDecimal, count: Int, buyTime: Long,
                            sellPrice: Option[BigDecimal] = None, sellTime: Option[Long] = None) {

  def sell(price: CurrentPrice) = this.copy(sellPrice = Some(price.bid), sellTime = Some(System.currentTimeMillis))

  def isSold = sellPrice.isDefined
}

object AssetTransaction {
  def buy(symbol: String, price: CurrentPrice, count: Int) = AssetTransaction(symbol, price.ask, count, System.currentTimeMillis)
}

case class Portfolio(name: String, cash: BigDecimal, transactions: List[AssetTransaction])

case class GetCurrentPrice(symbol: String)
case class CurrentPrice(symbol: String, ask: BigDecimal, bid: BigDecimal)

case class AssetSnapshot(symbol: String, buyPrice: BigDecimal, count: Int, symbolValue: BigDecimal)

case class PortfolioAnalysis(name: String, cash: BigDecimal, totalValue: BigDecimal, marketValue: BigDecimal,
                             profit: BigDecimal, profitPercentage: BigDecimal,
                             assets: List[AssetAnalysis])

case class AssetAnalysis(asset: AssetSnapshot, marketValue: BigDecimal, profit: BigDecimal, profitPercentage: BigDecimal)
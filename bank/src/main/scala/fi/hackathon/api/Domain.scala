package fi.hackathon.api

import akka.actor.ActorRef


case class BuyOrder(symbol: String, number: Int)
case class SellOrder(symbol: String, number: Int)

case class CreatePortfolio(name: String, value: BigDecimal)
case class ListPortfolios()
case class PortfolioList(portfolios: List[ActorRef])

case class PortfolioStatusRequest()
case class ValueRequest()


case class Asset(symbol: String, buyPrice: BigDecimal, count: Int, timestamp: Long)

case class Portfolio(name: String, cash: BigDecimal, assets: List[Asset])



case class GetCurrentPrice(symbol: String)
case class CurrentPrice(symbol: String, ask: BigDecimal, bid: BigDecimal)


case class AssetSnapshot(asset: Asset, symbolValue: BigDecimal)
case class PortfolioSnapshot(name: String, cash: BigDecimal, totalValue: BigDecimal, assets: List[AssetSnapshot])
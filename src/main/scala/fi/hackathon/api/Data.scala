package fi.hackathon.api

case class Data(data: String)
case class CreatePortfolio(name:String, value: BigDecimal)
case class BuyOrder(symbol: String, number: Int)

case class Asset(symbol: String, buyPrice: BigDecimal, count: Int)
case class AssetSnapshot(asset: Asset, symbolValue: BigDecimal)

case class Portfolio(name: String, cash: BigDecimal, assets: List[Asset])
case class PortfolioSnapshot(name: String, cash: BigDecimal, totalValue: BigDecimal, assets: List[AssetSnapshot])

case class ValueRequest()

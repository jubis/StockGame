package fi.hackathon.api

case class GetCurrentPrice(symbol: String)

case class CurrentPrice(symbol: String, ask: BigDecimal, bid: BigDecimal)

case class Asset(symbol: String, buyPrice: BigDecimal, count: Int)

case class AssetSnapshot(asset: Asset, symbolValue: BigDecimal)

case class Portfolio(name: String, cash: BigDecimal, assets: List[Asset])

case class PortfolioSnapshot(name: String, cash: BigDecimal, totalValue: BigDecimal, assets: List[AssetSnapshot])
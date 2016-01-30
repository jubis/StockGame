package fi.hackathon.api

case class GetCurrentPrice(symbol: String)
case class CurrentPrice(symbol: String, ask: BigDecimal, bid: BigDecimal)
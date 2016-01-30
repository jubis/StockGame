package me.matiass.actors.rest

import fi.hackathon.api.SellOrder
import me.matiass.actors.{TwitterFutureConversions, Rest, PortfolioRest}
import io.finch._
import io.finch.Endpoint
import me.matiass.actors.service.PortfolioService
import shapeless._
import TwitterFutureConversions._
import scala.concurrent.ExecutionContext.Implicits.global

class SellRest(portfolio: PortfolioRest)(implicit val portfolioService: PortfolioService) extends Rest {

  val list: Endpoint[String] = portfolio.byName / "sell"

  val sellOrderReader = param("symbol").map(SellOrder(_, 1))
  val create: Endpoint[String] = post(list ? sellOrderReader) { (portfolioName: String, sellOrder: SellOrder) =>
    portfolioService.doSell(portfolioName, sellOrder)
      .map {
        case true => "successful"
        case false => "failure"
      }
      .map(Ok(_))
      .toTwitter
  }

  override def endpoint = create
}
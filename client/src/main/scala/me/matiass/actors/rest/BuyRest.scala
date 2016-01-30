package me.matiass.actors

import fi.hackathon.api.{Portfolio, BuyOrder}
import io.finch._
import io.finch.Endpoint
import me.matiass.actors.service.PortfolioService
import shapeless._
import TwitterFutureConversions._
import scala.concurrent.ExecutionContext.Implicits.global

class BuyRest(portfolio: PortfolioRest)(implicit val portfolioService: PortfolioService) extends Rest {

  val list: Endpoint[String] = portfolio.byName / "buy"

  val buyOrderReader = (param("symbol") :: param("amount").as[Int]).as[BuyOrder]
  val create: Endpoint[String] = post(list ? buyOrderReader) { (portfolioName: String, buyOrder: BuyOrder) =>
    portfolioService.doBuy(portfolioName, buyOrder)
      .map {
        case true => "successful"
        case false => "failure"
      }
      .map(Ok(_))
      .toTwitter
  }

  override def endpoint = create
}


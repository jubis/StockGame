package me.matiass.actors

import fi.hackathon.api.PortfolioAnalysis
import io.finch._
import me.matiass.actors.service.PortfolioService
import shapeless._
import TwitterFutureConversions._
import scala.concurrent.ExecutionContext.Implicits.global


class PortfolioRest(implicit val service: PortfolioService) extends Rest {
  val portfolioReader: RequestReader[String] = param("name")

  val list: Endpoint[HNil] = "portfolio"

  val create: Endpoint[String] = post(list ? portfolioReader) { name: String =>
    service.createPortfolio(name)
    Ok(s"Created portfolio $name")
  }

  val byName: Endpoint[String] = list / string("name")

  val getByName: Endpoint[PortfolioAnalysis] = get(byName) { name: String =>
    service.getPortfolio(name)
      .map(Ok(_))
      .toTwitter
  }

  override def endpoint = create :+: getByName
}

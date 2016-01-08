package me.matiass.actors

import com.twitter.server.TwitterServer
import com.twitter.util.{Promise, Await}
import fi.hackathon.api.PortfolioSnapshot
import org.json4s.ext.JodaTimeSerializers
import shapeless._
import com.twitter.finagle.Http
import io.finch._
import TwitterFutureConversions._
import io.finch.json4s._
import org.json4s.DefaultFormats

import scala.concurrent.ExecutionContext.Implicits.global

class RestApi(service: PortfolioService) extends TwitterServer {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  val portfolioReader: RequestReader[String] = param("name")

  val portfolio: Endpoint[HNil] = "portfolio"

  val create: Endpoint[String] = post(portfolio ? portfolioReader) { name: String =>
    service.createPortfolio(name)
    Ok(s"Created portfolio $name")
  }

  val getByName: Endpoint[PortfolioSnapshot] = get(portfolio / string) { name: String =>
        service.getPortfolio(name)
          .map(Ok(_))
          .toTwitter
      }

  val endpoint: Endpoint[String :+: PortfolioSnapshot :+: CNil] = create :+: getByName

  val server = Http.server
    .serve(":8080", endpoint.toService)

  onExit { server.close() }

  Await.ready(adminHttpServer)
}

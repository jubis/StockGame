package me.matiass.actors

import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.Output
import org.json4s.ext.JodaTimeSerializers
import com.twitter.finagle.Http
import io.finch.json4s._
import org.json4s.DefaultFormats
import me.matiass.actors.service.Services

class RestApi extends TwitterServer {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  import Services._
  val portfolio = new PortfolioRest
  val buy = new BuyRest(portfolio)

  val coEndpoint = (portfolio.endpoint :+: buy.endpoint)
    .withHeader("Access-Control-Allow-Origin" -> "*")

  val server = Http.server
    .serve(":8080", coEndpoint.toService)

  onExit { server.close() }

  Await.ready(adminHttpServer)
}

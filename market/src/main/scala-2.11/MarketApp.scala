import akka.actor.{Status, Actor, ActorSystem, Props}
import fi.hackathon.api._
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

object MarketApp extends App {
  implicit val system = ActorSystem("MarketSystem")
  val actor = system.actorOf(Props[MarketActor], name = "market")
}

class MarketActor extends Actor {

  override def receive: Receive = {
    case msg: GetCurrentPrice => {

      println(s"Request for price of ${msg.symbol}")

      val price = getCurrentPrice(msg.symbol)

      println(s"Price $price")

      price match {
        case Success(price) => sender ! price
        case Failure(e) => sender ! Status.Failure(e)
      }
    }
  }

  def yahooQuery(symbol: String): String = s"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%3D%22$symbol%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"

  def getCurrentPrice(symbol: String): Try[CurrentPrice] = {
    Try {
      val url = yahooQuery(symbol)
      val jsonStr = scala.io.Source.fromURL(url).mkString
      val json = Json.parse(jsonStr)
      val ask = (json \ "query" \ "results" \ "quote" \ "Ask").as[String]
      val bid = (json \ "query" \ "results" \ "quote" \ "Bid").as[String]
      CurrentPrice(symbol, BigDecimal(ask), BigDecimal(bid))
    }
  }
}
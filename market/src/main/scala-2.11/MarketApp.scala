import akka.actor.{Actor, ActorSystem, Props, Status}
import dispatch.StatusCode
import fi.hackathon.api._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object MarketApp extends App {
  implicit val system = ActorSystem("MarketSystem")
  val actor = system.actorOf(Props[MarketActor], name = "market")
}

class MarketActor extends Actor {
  def timeMs: Long = System.currentTimeMillis
  val TIMEOUT: Long = 4500

  def handleTime[T](f: Future[T], symbol: String): Future[T] = {
    val startTime = timeMs

    f
      .map(result => (result, timeMs - startTime))
      .andThen {
        case Success((_, time: Long)) if time > TIMEOUT => println(s"Request for $symbol took too long (${time}ms)")
        case Success((_, time: Long)) => println(s"Request for $symbol took ${time}ms")
      }
      .filter { case (_, time) => time <= TIMEOUT }
      .map(_._1)
  }

  override def receive: Receive = {
    case msg: GetCurrentPrice => {

      val asker = sender
      val symbol = msg.symbol

      println(s"Request for price of $symbol")

      val priceF = getCurrentPrice(symbol)

      handleTime(priceF, symbol)
        .onComplete {
          case Success(price) => asker ! price
          case Failure(StatusCode(code)) => asker ! Status.Failure(new Exception(s"Marketplace didn't like our request or failed itself ($code)"))
          case Failure(_) => asker ! Status.Failure(new Exception(s"Something went wrong with the query"))
        }
    }
  }

  def yahooQuery(symbol: String): String = s"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%3D%22$symbol%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"

  import dispatch._
  val http = new Http

  def getCurrentPrice(symbol: String): concurrent.Future[CurrentPrice] = {
    def parseResult(jsonStr: String): CurrentPrice = {
      val json = Json.parse(jsonStr)
      val ask = (json \ "query" \ "results" \ "quote" \ "Ask").as[String]
      val bid = (json \ "query" \ "results" \ "quote" \ "Bid").as[String]
      CurrentPrice(symbol, BigDecimal(ask), BigDecimal(bid))
    }

    val urlString = yahooQuery(symbol)
    val responseF = http(url(urlString) OK as.String)

    responseF.map(parseResult)
  }
}

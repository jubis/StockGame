package stockgame.bank

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import fi.hackathon.api._

import scala.concurrent.Await

object Main extends App {

  implicit val system = ActorSystem("BankSystem")


  val market = resolve(system.actorSelection("akka.tcp://MarketSystem@market:8888/user/market"))

  val analyser = system.actorOf(Props.create(classOf[PortfolioAnalyzer], market), name = "analyzer")

  val bank = system.actorOf(Props.create(classOf[BankActor], market, analyser), name = "bank")

  def resolve(selection: ActorSelection): ActorRef = {
    val resolveTimeout = Timeout(5, TimeUnit.SECONDS)
    val awaitTimeout = Timeout(5, TimeUnit.SECONDS)

    Await.result(selection.resolveOne()(resolveTimeout), awaitTimeout.duration)
  }
}


class BankActor(market: ActorRef, analyser: ActorRef) extends Actor {

  override def receive = LoggingReceive {
    case msg: CreatePortfolio => {
      println("Create request")
      context.actorOf(Props.create(classOf[PortfolioActor], market, analyser, msg.name, msg.value), name = msg.name)
    }
    case _ => println("Something else")
  }
}

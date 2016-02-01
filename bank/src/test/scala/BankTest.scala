import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Actor, Props}
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import akka.util.Timeout
import fi.hackathon.api._
import org.scalatest.{BeforeAndAfter, WordSpecLike, Matchers, BeforeAndAfterAll}
import stockgame.bank.PortfolioActor
import akka.pattern.ask

class MySpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  def this() = this(ActorSystem("Test"))

  var portfolio: ActorRef = null

  before {
    val market = system.actorOf(Props(classOf[MarketMock]))
    portfolio = system.actorOf(Props(classOf[PortfolioActor], market, "test", BigDecimal(1000.00)))
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Portfolio" must {

    /*"decrease cash when buing asset" in {
      portfolio ! BuyOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.cash shouldBe BigDecimal(950.00)}
    }

    "add asset when buying it" in {
      portfolio ! BuyOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.assets.map(TestAsset.apply) should contain(TestAsset("MSSF", BigDecimal(5.0), 10))}
    }

    "not change if no money when buying assets" in {
      portfolio ! BuyOrder("MSSF", 10000)
      expectMsgPF() { case pf: Portfolio => pf.cash shouldBe BigDecimal(1000.00)}
    }

    "not change if no response from market" in {
      portfolio ! BuyOrder("NOCON", 10)
      expectMsgPF(Timeout(6, TimeUnit.SECONDS).duration) { case pf: Portfolio => pf.cash shouldBe BigDecimal(1000.00)}
    }

    "increase cash when selling asset" in {
      portfolio ! BuyOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.cash shouldBe BigDecimal(950.00)}
      portfolio ! SellOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.cash shouldBe BigDecimal(990.00)}
    }

    "remove asset when selling it" in {
      portfolio ! BuyOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.assets.map(TestAsset.apply) should contain(TestAsset("MSSF", BigDecimal(5.0), 10))}
      portfolio ! SellOrder("MSSF", 10)
      expectMsgPF() { case pf: Portfolio => pf.assets.size shouldBe 0}
    }*/
  }

}

case class TestAsset(symbol: String, buyPrice: BigDecimal, count: Int)

object TestAsset {
  //def apply(asset: Asset): TestAsset = TestAsset(asset.symbol, asset.buyPrice, asset.count)
}

class MarketMock() extends Actor {

  def receive = {
    case GetCurrentPrice("NOCON") => unhandled()
    case GetCurrentPrice(symbol) => sender ! Some(CurrentPrice(symbol, BigDecimal(5.0), BigDecimal(4.0)))
  }
}

package me.matiass.actors

import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._

object ActorBase {
  val bankSystemBase = "akka.tcp://BankSystem@bank:5555/user/"
  val system = ActorSystem("ClientSystem")

  implicit val timeout = Timeout(5 seconds)
}

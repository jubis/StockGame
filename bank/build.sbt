name := "StockGame Bank"

version := "1.0"

scalaVersion := "2.11.7"

mainClass in assembly := Some("stockgame.bank.Main")
assemblyJarName in assembly := "bank.jar"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.1"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.1"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.0-M3"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.1"

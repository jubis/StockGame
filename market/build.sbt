name := "StockGame Market"

version := "1.0"

scalaVersion := "2.11.7"

mainClass in assembly := Some("MarketApp")
assemblyJarName in assembly := "market.jar"

libraryDependencies +=  "com.typesafe.akka" %% "akka-actor" % "2.4.1"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.1"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.0-M3"

libraryDependencies +=  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3"
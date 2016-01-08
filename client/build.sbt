name := "AkkaHackathon"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "twttr" at "https://maven.twttr.com/"

libraryDependencies ++= Seq(
  "com.twitter" %% "twitter-server" % "1.16.0",
  "com.github.finagle" %% "finch-core" % "0.9.3",
  "com.github.finagle" %% "finch-json4s" % "0.9.3",
  "com.typesafe.akka" %% "akka-remote" % "2.4.1",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

FROM java:openjdk-8-jre

COPY target/scala-2.11/market.jar /stockgame/market.jar

WORKDIR /stockgame
CMD java -jar -Dakka.remote.netty.tcp.hostname=$AKKA_HOSTNAME market.jar

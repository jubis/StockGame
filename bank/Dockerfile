FROM java:openjdk-8-jre

COPY target/scala-2.11/bank.jar /stockgame/bank.jar

WORKDIR /stockgame
CMD java -jar -Dakka.remote.netty.tcp.hostname=$AKKA_HOSTNAME bank.jar

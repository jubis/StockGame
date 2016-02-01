FROM java:openjdk-8-jre

ENV NPM_CONFIG_LOGLEVEL info
ENV NODE_VERSION 5.5.0
RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.gz" \
    && tar -xzf "node-v$NODE_VERSION-linux-x64.tar.gz" -C /usr/local --strip-components=1 \
    && npm install -g forever

COPY front /stockgame/front
COPY target/scala-2.11/client.jar /stockgame/client.jar

CMD cd /stockgame/front && npm run serve \
    && cd /stockgame && java -jar -Dakka.remote.netty.tcp.hostname=$AKKA_HOSTNAME client.jar

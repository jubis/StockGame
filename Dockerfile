from hseeberger/scala-sbt

ENV NPM_CONFIG_LOGLEVEL info
ENV NODE_VERSION 5.5.0

RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.gz" \
  && tar -xzf "node-v$NODE_VERSION-linux-x64.tar.gz" -C /usr/local --strip-components=1 \
	&& npm install -g forever

EXPOSE 5000

COPY . /stockgame

RUN cd /stockgame/client && sbt -v update
RUN cd /stockgame/bank && sbt -v update
RUN cd /stockgame/market && sbt -v update

CMD cd /stockgame && ./start.sh

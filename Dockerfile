from hseeberger/scala-sbt

RUN apt-get install -y node && \
	npm install -g forever

EXPOSE 5000

ADD . /stockgame
COMMAND cd market && sbt run & && cd ../bank && sbt run & && cd ../client && sbt run & && npm run serve' 




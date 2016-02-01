#!/bin/bash -x

cd /stockgame/client/front
npm run serve

cd /stockgame/market
sbt run &
cd /stockgame/bank
sbt run &
cd /stockgame/client
sbt run &

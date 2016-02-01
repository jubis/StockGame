cd market
sbt assembly
docker build -t jubis/sg-market .

cd ../client
sbt assembly
docker build -t jubis/sg-client .

cd ../bank
sbt assembly
docker build -t jubis/sg-bank .

docker stop pingserver
docker stop pingclient
docker stop data
docker network rm myNetwork
docker rm pingserver
docker rm pingclient
docker rm data

javac ./rmi/client/*.java
javac ./rmi/server/*.java

docker build -t rmi ./rmi

docker network create -d bridge myNetwork
docker create -v /data --name data rmi /bin/true


docker run -itd --net=myNetwork  --name pingserver rmi --volumes-from data java -cp ./data Server 7000
docker run -itd --net=myNetwork  --name pingclient rmi --volumes-from data java -cp ./data Client 7000
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

pingserver_ip='docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingserver'
docker run -d --net=myNetwork  --name pingserver --volumes-from data rmi java data.server 7000
docker run -d --net=myNetwork  --name pingclient --volumes-from data rmi java data.client pingserver_ip 7000
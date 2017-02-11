docker stop pingserver
docker stop pingclient
docker stop files
docker network rm myNetwork
docker rm files
docker rm pingserver
docker rm pingclient

javac ./docker/server/*.java
javac ./docker/client/*.java

docker build -t rmi_docker ./rmi_docker

docker network create -d bridge myNetwork

pingserver_ip=`docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingserver`

docker run -d --net=myNetwork  --name pingserver --volumes-from files rmi_docker java ./files . 7000
docker run -d --net=myNetwork  --name pingclient --volumes-from files rmi_docker java ./files .CatClient pingserver_ip catserver 7000
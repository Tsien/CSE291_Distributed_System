#!/bin/bash
clear
docker build -t serverimg ./server
printf "\n>>Lab0: succeed to build an image for server...\n\n"

docker build -t clientimg ./client
printf "\n>>Lab0: succeed to build an image for client...\n\n"

docker build -t dataimg ./data
printf "\n>>Lab0: succeed to build an image for data volume...\n\n"

docker run -d -v /data --name dbdata dataimg echo Data-only container
printf "\n>>Lab0: succeed to run a data volume container...\n\n"

docker run -itd --volumes-from dbdata --name catserver serverimg
printf "\n>>Lab0: succeed to mount the data volume in the server...\n\n"

docker run -itd --volumes-from dbdata --name catclient --link catserver clientimg
printf "\n>>Lab0: succeed to mount the data volume in the client...\n\n"

printf "\n>>Lab0: please wait for 30 seconds to see the logs...\n\n"
sleep 32
printf "\n>>Lab0: Here you go: \n"
printf "================================================================================\n\n"
docker logs catclient
printf "\n================================================================================\n"

printf "\n>>Lab0: start to clear up...\n\n"

docker rm $(docker ps -a -q)
docker rmi serverimg
docker rmi clientimg
docker rmi dataimg

printf "\n>>Lab0: Done! \n"

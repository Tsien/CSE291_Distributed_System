#!/bin/bash

clear

#####################################################################
# Build images:
#####################################################################

docker build -t rmi_img .

printf "\n\n================================================================================\n\n"
printf "Finish building images...\n\n"

#####################################################################
# Start running containers
#####################################################################

# docker run -itd --name "test" rmi_img /bin/bash

docker run -itd --name "server" rmi_img java -cp . pingPong.server.Server 7000

docker run -itd --name "client" --link server rmi_img java -cp . pingPong.client.Client server 7000

#####################################################################
# Display the result
#####################################################################

sleep 3
printf "================================================================================\n\nClient logs:\n\n"
docker logs client
printf "\n================================================================================\n\nServer logs:\n\n"
docker logs server
printf "\n================================================================================\n"

#####################################################################
# Clean up
#####################################################################
printf "\n>>Start to clear up...\n\n"

docker rm -f $(docker ps -a -q)
docker rmi rmi_img

#find . -name '*.class' -delete

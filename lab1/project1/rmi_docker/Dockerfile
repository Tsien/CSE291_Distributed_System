FROM ubuntu

RUN apt-get update && apt-get install -y default-jdk

RUN mkdir /files
VOLUME /files

COPY ./client/*.class /files/
COPY ./server/*.class /files/

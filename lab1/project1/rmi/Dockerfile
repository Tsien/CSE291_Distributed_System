FROM ubuntu

RUN apt-get update && apt-get install -y default-jdk

RUN mkdir /data
VOLUME /data

COPY ./client/*.class /data/
COPY ./server/*.class /data/

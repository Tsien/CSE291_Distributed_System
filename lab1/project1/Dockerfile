FROM java:7

# FROM harisekhon/ubuntu-java

COPY . /usr/src/code
WORKDIR /usr/src/code

RUN find . -name "*.java" -print | xargs javac

# RUN apt-get update \
#	&& apt-get install sudo \
#	&& sudo apt-get -y install build-essential





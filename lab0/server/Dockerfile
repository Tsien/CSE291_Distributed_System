# Pull base image.
FROM ubuntu:14.04
MAINTAINER Feichao Qian <feqian@ucsd.edu>

# Install java8
RUN apt-get update \
 && apt-get upgrade -y \
 && apt-get install -y software-properties-common \
 && add-apt-repository ppa:webupd8team/java -y \
 && apt-get update \
 && echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
 && apt-get install -y oracle-java8-installer \
 && apt-get clean

# Add server code
ADD ./catserver.java /code/

# complie and run the server
CMD [ "sh", "-c", "javac /code/catserver.java && cd code && java catserver /data/string.txt 2000" ]
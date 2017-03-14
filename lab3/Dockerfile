# Pull base image.
FROM gelog/hadoop
MAINTAINER Feichao Qian <feqian@ucsd.edu>

# COPY files
COPY ./Data/ /MyData/
COPY ./Code/ /MyCode/

# Compile 

RUN javac -classpath `yarn classpath` -d ./MyCode ./MyCode/*.java \
	&& jar -cvf bigrams.jar -C ./MyCode . \
	&& mv bigrams.jar /usr/local/hadoop/share/hadoop/mapreduce/








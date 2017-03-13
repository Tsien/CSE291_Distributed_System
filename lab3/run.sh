#!/bin/bash

#=========================================================================
# Hadoop Configuration 
NumDataNodes = 4


clear


##########################################################################
# Build an image from Dockerfile
##########################################################################
printf "\n\n============================================================="
printf "\n>>Start to build the image...\n\n"
docker build -t myhadoop .


##########################################################################
# Task 1: start up a Hadoop cluster 
##########################################################################
printf "\n\n============================================================="
printf "\n>>Start to build a Hadoop cluster ...\n\n"

#=========================================================================
# 	1. Starting the NameNode

#docker run -itd --name hdfs-namenode \
#    -h hdfs-namenode -p 50070:50070 \
#    myhadoop /bin/bash
docker run -d --name hdfs-namenode \
    -h hdfs-namenode -p 50070:50070 \
    myhadoop hdfs namenode #&& \
#docker logs -f hdfs-namenode

#=========================================================================
# 	2. Starting four DataNodes
for (( i = 1; i <= 4; i++ )); do
	#statements
	docker run -d --name hdfs-datanode"$i" \
	    -h hdfs-datanode1 -p 5007"$i":5007"$i" \
	    --link=hdfs-namenode:hdfs-namenode \
	    myhadoop hdfs datanode #&& \
	#docker logs -f "hdfs-datanode$i"	
done

#=========================================================================
# 	3. Starting YARN
# TODO: try docker networking here?
docker run -d --name yarn \
        -h yarn \
        -p 8088:8088 \
        -p 8042:8042 \
        --link=hdfs-namenode:hdfs-namenode \
        --link=hdfs-datanode1:hdfs-datanode1 \
        --link=hdfs-datanode1:hdfs-datanode2 \
        --link=hdfs-datanode1:hdfs-datanode3 \
        --link=hdfs-datanode1:hdfs-datanode4 \
        -v $HOME/data/hadoop/hdfs:/data \
        myhadoop start-yarn.sh #&& \
#docker logs -f yarn

##########################################################################
# Task 2: Run WordCount example to Confirm cluster is working
##########################################################################
printf "\n\n============================================================="
printf "\n>>Start to Run WordCount example ...\n\n"

#=========================================================================
#	1. Submit a Map Reduce job: Put some data in HDFS
docker run --rm \
        --link=hdfs-namenode:hdfs-namenode \
        --link=hdfs-datanode1:hdfs-datanode1 \
        --link=hdfs-datanode1:hdfs-datanode2 \
        --link=hdfs-datanode1:hdfs-datanode3 \
        --link=hdfs-datanode1:hdfs-datanode4 \
        myhadoop \
        hadoop fs -put /MyData/text.txt /text.txt

#=========================================================================
#	2. Start wordcount example
docker run --rm \
        --link yarn:yarn \
        --link=hdfs-namenode:hdfs-namenode \
        myhadoop \
        hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.0.jar wordcount  /text.txt /result

#=========================================================================
#	3. Check the result
docker run --rm --link=hdfs-namenode:hdfs-namenode \
        --link=hdfs-datanode1:hdfs-datanode1 \
        --link=hdfs-datanode1:hdfs-datanode2 \
        --link=hdfs-datanode1:hdfs-datanode3 \
        --link=hdfs-datanode1:hdfs-datanode4 \
        myhadoop \
        hadoop fs -cat /result/\*


##########################################################################
# Task 3: bigrams stats
##########################################################################
printf "\n\n============================================================="
printf "\n>>Start to count bigrams ...\n\n"

#=========================================================================
#	1. Start bigram counter
docker run --rm \
        --link yarn:yarn \
        --link=hdfs-namenode:hdfs-namenode \
        myhadoop \
        hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.0.jar wordcount  /text.txt /result

#=========================================================================
#	2. Check the result
docker run --rm --link=hdfs-namenode:hdfs-namenode \
        --link=hdfs-datanode1:hdfs-datanode1 \
        --link=hdfs-datanode1:hdfs-datanode2 \
        --link=hdfs-datanode1:hdfs-datanode3 \
        --link=hdfs-datanode1:hdfs-datanode4 \
        myhadoop \
        hadoop fs -cat /result/\*

#=========================================================================
#	3. Output stats



##########################################################################
# Clean up
##########################################################################

printf "\n\n============================================================="
printf "\n>>Start to clear up...\n\n"
docker rm -f $(docker ps -a -q)
docker rmi myhadoop



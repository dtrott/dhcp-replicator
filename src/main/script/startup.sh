#!/bin/bash

DIR=`dirname $0`

CLASSPATH='.'
for LIB in $DIR/lib/*
do
    CLASSPATH=$CLASSPATH:$LIB
done

java -cp $CLASSPATH com.davidtrott.util.dns.replicator.DhcpReplicator $*

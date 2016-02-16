#!/bin/bash
COUNT=$1
if [[ -z "$COUNT" ]];then
	  COUNT=1
  fi

echo "" > pids

for (( c=1; c<=COUNT; c++ ))
do
	export LBS_INSTANCE="$c"
	echo "starting server no $c"
	nohup node --max-old-space-size=2048 . > log.txt 2>&1 & echo $! >> pids
done

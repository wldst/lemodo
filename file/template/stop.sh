#!/bin/sh
PID=$(ps -ef | grep #{jar} | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
  echo jar is already stopped
else
  echo kill $PID
  kill $PID
fi


 

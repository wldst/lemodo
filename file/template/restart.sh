#!/bin/sh
ROOT="#{dir}"

PID=$(ps -ef | grep #{jar} | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
  echo jar is already stopped
else
  echo kill $PID
  kill $PID
fi
echo -e "\033[0;31m  #{name}服务已关闭 \033[0m"
cd $ROOT

nohup java -jar -Xms256M -Xmx256M "$ROOT"/#{jar} > #{name}.out &  > /dev/null 2>&1 
 
echo $! > "$ROOT"/application.pid
echo -e "\033[0;31m  #{name}服务已重启 \033[0m"
tail -900f  #{name}.out

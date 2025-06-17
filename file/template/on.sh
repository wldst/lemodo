#!/bin/sh
ROOT="#{dir}"
 
cd $ROOT

nohup java -jar -Xms256M -Xmx256M "$ROOT"/#{jar} > #{name}.out &  > /dev/null 2>&1 
 
echo $! > "$ROOT"/application.pid

tail -900f  #{name}.out

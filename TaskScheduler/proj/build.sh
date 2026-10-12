#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java")
printf 'Main-Class: com.wipro.scheduler.service.TaskSchedulerService\n' > manifest.txt
jar cfm TaskScheduler.jar manifest.txt -C out .

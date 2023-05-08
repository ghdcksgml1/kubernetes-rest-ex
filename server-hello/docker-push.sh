#!/bin/bash

./gradlew clean build
docker build -t webserver-hello .
docker tag webserver-hello ghdcksgml1/spring-hello
docker push ghdcksgml1/spring-hello

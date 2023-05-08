#!/bin/bash

./gradlew clean build
docker build -t webserver-rest .
docker tag webserver-rest ghdcksgml1/spring-rest
docker push ghdcksgml1/spring-rest


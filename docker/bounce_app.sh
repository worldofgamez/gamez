#!/bin/bash

docker rm -f gamez_app > /dev/null 2> /dev/null

docker run -d --restart=always -p 3000:3000 --name=gamez_app --link gamez_redis:redis gamez/app:latest


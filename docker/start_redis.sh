#!/bin/bash

# Start Redis

# if the db doesn't exist, start it
docker inspect gamez_redis > /dev/null 2> /dev/null || \
    docker run -d --restart=always --name=gamez_redis -p 56379:6379 redis:latest

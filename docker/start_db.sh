#!/bin/bash

# Start the database

# if the db doesn't exist, start it
docker inspect gamez_db > /dev/null 2> /dev/null || \
    docker run -d --restart=always --name=gamez_db -p 55432:5432 postgres:latest

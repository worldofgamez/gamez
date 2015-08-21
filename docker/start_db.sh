#!/bin/bash

# Start the database

# if the db doesn't exist, start it
docker inspect wog_db > /dev/null 2> /dev/null || \
    docker run -d --restart=always --name=wog_db -p 55432:5432 postgres:latest

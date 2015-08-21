#!/bin/bash

# Start psql and connect to the DB

docker run -ti --rm --link wog_db postgres:latest psql -h wog_db -U postgres

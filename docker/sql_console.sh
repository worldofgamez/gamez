#!/bin/bash

# Start psql and connect to the DB

rlwrap docker run -ti --rm --link gamez_db postgres:latest psql -h gamez_db -U postgres

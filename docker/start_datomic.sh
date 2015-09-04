#!/bin/bash

if [[ -z $(docker images | grep gamez | grep datomic) ]]; then
    `dirname $0`/build_datomic.sh
fi

# if the db doesn't exist, start it
docker inspect gamez_datomic > /dev/null 2> /dev/null || \
    docker run -d --restart=always --name=gamez_datomic -p 4334:4334 \
           -p 4335:4335 -p 4336:4336 gamez/datomic

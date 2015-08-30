#!/bin/bash

cd `dirname $0`/..

for x in $(ls datomic/dat*.zip); do
    n=$x
done

WORKING_DIR=$(mktemp -d /tmp/wog.XXXXXX) || exit 1

cp $n $WORKING_DIR

cp docker/datomic_dockerfile $WORKING_DIR/Dockerfile

cd $WORKING_DIR

unzip -q *.zip

rm *.zip

mv datomic-free* datomic

cat datomic/config/samples/free-transactor-template.properties | sed "s/host=localhost/host=0.0.0.0/" > datomic/free-transactor.properties

docker build --tag=wog/datomic .

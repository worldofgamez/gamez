#!/bin/bash

# Build the app on the target machine

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )

cd ${DIR}

WORKING_DIR=$(mktemp -d /tmp/gamez.XXXXXX) || exit 1

echo "Working DIR: ${WORKING_DIR}"

chmod og-rwx ${WORKING_DIR}

echo "Starting app build"

lein do clean, uberjar && cp target/gamez.jar ${WORKING_DIR} && \
    cp ${DIR}/docker/app.docker ${WORKING_DIR}/Dockerfile && \
    cp ${DIR}/resources/props/the_prod.props ${WORKING_DIR} && \
    cd ${WORKING_DIR} && echo "About to build Docker container" && \
    docker build -t gamez/app:latest . && \
    cd ${DIR} && \
    rm -rf ${WORKING_DIR}


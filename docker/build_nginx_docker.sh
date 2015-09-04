#!/bin/bash

# Build a HAProxy instance. We assume that you're running docker-machine to point
# To the host where the HAProxy instance will run

if [[  -z "$1"  || ! -f $1  ]]; then
  echo "This command requires a second parameter that points to the .pem file"
  exit 99
  else
  echo "it's $1"
fi

if [[ -z "$2" ]] ; then
    echo "This command requires the domain of the server as the third param"
    echo "e.g. mygamez.com"
    exit 99
else
    echo "Domain: $2"
fi

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )

echo $DIR

# Create a working directory so we can copy secrets into it
# So make it private to me

WORKING_DIR=$(mktemp -d /tmp/gamez.XXXXXX) || exit 1

echo "Working dir ${WORKING_DIR}"

chmod og-rwx ${WORKING_DIR}

cp -r ${DIR}/docker/nginx_conf ${WORKING_DIR}

perl -ne 'local $/=undef; $x = <STDIN>; print "$1\n" if ($x =~ m/([^\n]*PRIVATE.*PRIVATE[^\n]*)/s);' \
     < $1 > ${WORKING_DIR}/nginx_conf/site.key

cp $1 ${WORKING_DIR}/nginx_conf/site.pem

chmod og-rwx ${WORKING_DIR}/nginx_conf/site.key

chmod og-rwx ${WORKING_DIR}/nginx_conf/site.pem


cp ${DIR}/docker/nginx.docker ${WORKING_DIR}/Dockerfile

cd ${WORKING_DIR}/nginx_conf

openssl dhparam -outform PEM -out dhparams.pem 1024

cd ${WORKING_DIR}/nginx_conf/conf.d

sed s/SERVER_NAME/$2/g < default.conf > d2.conf

rm default.conf

mv d2.conf default.conf

cd ${WORKING_DIR}

docker build -t gamez/nginx:latest .

cd ${DIR}

rm -rf ${WORKING_DIR}

docker rm -f nginx-runner > /dev/null 2> /dev/null

docker run -d --name=nginx-runner --net=host --restart=always gamez/nginx:latest

echo "Done"

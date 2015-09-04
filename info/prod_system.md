# Setting up a Production system

## Docker machine

## Set up mount points on host

    /var/lib/container_data/pg

## Run postgres

    docker run -d --restart=always --name postgres -v /var/lib/container_data/pg:/var/lib/postgresql  -d postgres


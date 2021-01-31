![Frontend CI](https://github.com/ComputerScienceHouse/DEaDASS/workflows/Node%20CI%20for%20the%20frontend/badge.svg)
![Backend CI](https://github.com/ComputerScienceHouse/DEaDASS/workflows/Node%20CI%20for%20the%20backend/badge.svg)
![Backend Docker Build](https://github.com/ComputerScienceHouse/DEaDASS/workflows/Backend%20Docker%20Build/badge.svg)

# The rewrite
DEaDASS was originally started in Java with spring, but is being rewritten in
typescript with node. This readme will be updated to reflect this change eventually.

For backend dev setup, see [backend/README.md](./backend/README.md).

# DEaDASS
Database Execution and Deletion Administrative Self Service

DEaDASS is a database management system to allow automated creation and
deletion of various database instances, as well as eventually replicating and
replacing the functionality of [phpMyAdmin](https://www.phpmyadmin.net) and
[phpPgAdmin](http://phppgadmin.sourceforge.net/doku.php).
It is written in Java with Spring.

This repository hosts the DEaDASS API, the frontend may be found [here](https://github.com/ComputerScienceHouse/DEaDASS-UI).

## Local Development
You're going to need [java](https://www.java.com/en/) and [gradle](https://gradle.org/). Recommend using Intellij for ease of use.
Java can be any Java newer than 8 and either Oracle or OpenJDK.

### Setup Environment

Set Environment variables
```sh
# A comma or space separated list of origins e.g. "https://deadass.mycompany.net, http://localhost:8000"
export ALLOWED_ORIGINS=

## Mail configuration
export ADMIN_UID=   # The mail user to notify of standing approval requests

# DEaDASS's mail login information
export MAIL_HOST=
export MAIL_PASSWORD=
export MAIL_USER=

## Manager DB (postgres) credentials
export MANAGER_URI=
export MANAGER_PASSWORD=
export MANAGER_USER=

## Managed DB server credentials
export MONGO_URI=
export MYSQL_PASSWORD=

export MYSQL_URI=
export MYSQL_USER=

export POSTGRESS_URI=
export POSTGRESS_PASSWORD=
export POSTGRESS_USER=
```

### Local Databases

Here's a script using [podman](https://podman.io/) to set up the 3 databases you need for local testing

```sh
export POSTGRES_USER=deadass
export POSTGRES_PASSWORD=

export MONGO_INITDB_ROOT_USERNAME=deadass
export MONGO_INITDB_ROOT_PASSWORD=

export MYSQL_ROOT_PASSWORD=
export MYSQL_USER=deadass
export MYSQL_PASSWORD=


sudo podman pod create --name=DEaDASS-dbs  --publish 5432,3306,27017
sudo podman run --name=postgres -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD -e POSTGRES_USER=$POSTGRES_USER  -dt --pod=DEaDASS-dbs postgres
sudo podman run --name=mongo -e MONGO_INITDB_ROOT_USERNAME=$MONGO_INITDB_ROOT_USERNAME -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_INITDB_ROOT_PASSWORD  -dt --pod=DEaDASS-dbs mongo
sudo podman run --name=mysql -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -e MYSQL_USER=$MYSQL_USER -e MYSQL_PASSWORD=$MYSQL_PASSWORD  -dt --pod=DEaDASS-dbs mysql
```

You'll also need to setup the tables in the manager database (this can just be a database in the postgres dev db). This will eventually be managed by the application, but for the moment you'll need the following SQL (assuming the role in postgress is named deadass):
```sql
create table pools
(
    owner varchar(32) default 'rtp'::character varying not null,
    is_group boolean default true not null,
    num_limit integer default 5 not null,
    id serial not null
        constraint pools_pkey
            primary key,
    title varchar(32) default 'default'::character varying

);

alter table pools owner to deadass;

create unique index pools_id_uindex
    on pools (id);

create table databases
(
    pool integer not null
        constraint pool_id
            references pools,
    name varchar(80) not null
        constraint databases_pkey
            primary key,
    purpose text,
    type varchar(16) not null,
    approved boolean default false not null

);

alter table databases owner to deadass;

create unique index databases_name_uindex
    on databases (name);

create table users
(
    database varchar(80) not null
        constraint users_databases_name_fk
            references databases,
    owner varchar(32) default 'rtp'::character varying not null,
    is_group boolean default true not null,
    username varchar(32) not null,
    last_reset timestamp,
    constraint users_pk
        primary key (database, username)

);

alter table users owner to deadass;
```

# DEaDASS UI

Database Execution and Deletion Administrative Self Service

DEaDASS is a database management system to allow automated creation and deletion of various database instances, as well as eventually replicating and replacing the functionality of [phpMyAdmin](https://www.phpmyadmin.net) and [phpPgAdmin](http://phppgadmin.sourceforge.net/doku.php).
It is written in Java with Spring.

This repository hosts the DEaDASS UI, the backend may be found [here](https://github.com/ComputerScienceHouse/DEaDASS).

## Local Development
You're going to need [node](https://nodejs.org/en/) and ideally use [nvm](https://github.com/nvm-sh/nvm).

### Setup with nvm

```
nvm install
nvm use
npm install
```

### Set up API
By default the application will try to make calls against the production [DEaDASS API](https://github.com/ComputerScienceHouse/DEaDASS). If you need to test against a local backend, you're going to need to override the environment variables

```
cp .env .env.local
```

Edit `REACT_APP_API_ROUTE` to point to `http://localhost:8080`

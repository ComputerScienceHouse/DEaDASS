# DEaDASS
Database Execution and Deletion Administrative Self Service

DEaDASS is a database management system to allow automated creation and deletion of various database instances, as well as eventually replicating and replacing the functionality of [phpMyAdmin](https://www.phpmyadmin.net) and [phpPgAdmin](http://phppgadmin.sourceforge.net/doku.php).
It is written in Java with Spring.

This repository hosts the DEaDASS API, the frontend may be found [here](https://github.com/ComputerScienceHouse/DEaDASS-UI).

## Local Development
You're going to need [java](https://www.java.com/en/) and [gradle](https://gradle.org/). Recommend using Intellij for ease of use.
Java can be any Java newer than 8 and either Oracle or OpenJDK.

### Setup Environment

Set Environment variables
```
export ALLOWED_ORIGINS=
export ADMIN_UID=
export MAIL_HOST=
export MAIL_PASSWORD=
export MAIL_USER=
export MANAGER_URI=
export MANAGER_PASSWORD=
export MANAGER_USER=
export MONGO_URI=
export MYSQL_PASSWORD=
export MYSQL_URI=
export MYSQL_USER=
export POSTGRESS_PASSWORD=
export POSTGRESS_URI=
export POSTGRESS_USER=
```

### Local Databases

Here's a script using [podman](https://podman.io/) to set up the 3 databases you need for local testing

```
export POSTGRES_USER=deadass
export POSTGRES_PASSWORD=

export MONGO_INITDB_ROOT_USERNAME=deadass
export MONGO_INITDB_ROOT_PASSWORD=

export MYSQL_ROOT_PASSWORD=
export MYSQL_USER=deadass
export MYSQL_PASSWORD=


#sudo podman pod create --name=DEaDASS-dbs # --publish 5432,3306,27017
sudo podman run --name=postgres -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD -e POSTGRES_USER=$POSTGRES_USER -p 5432 -dt  postgres
sudo podman run --name=mongo -e MONGO_INITDB_ROOT_USERNAME=$MONGO_INITDB_ROOT_USERNAME -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_INITDB_ROOT_PASSWORD -p 27017 -dt  mongo
sudo podman run --name=mysql -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -e MYSQL_USER=$MYSQL_USER -e MYSQL_PASSWORD=$MYSQL_PASSWORD -p 3306 -dt  mysql
```
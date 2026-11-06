DEaDASS 2 - In case you didn't learn your lesson the first time
===============================================================

Database Execution and Deletion Administrative Self Service

DEaDASS is a database management system to allow automated creation and deletion of various database instances, as well as eventually replicating and replacing the functionality of phpMyAdmin and phpPgAdmin. It is written in Flask.

Development
-----------

## Spinning up

Docker / Podman is the recommended method of running this.

```sh
podman compose up --watch
```

## Config
To easily configure your workspace, copy the `.env.example` into a new `.env` file.

### Add OIDC Config
Reach out to an RTP to get OIDC credentials that will allow you to develop locally behind OIDC auth.

You can also fetch dev credentials from the wiki (https://wiki.csh.rit.edu/wiki/SSO).

### Database
You can either develop using the dev database, or use the local database provided in the docker compose file.
(But like if you're editing DEaDASS you probably don't want to be relying on DEaDASS).

In order to use a fresh or outdated database, you must upgrade it via the command below.

```sh
podman exec deadass flask db upgrade
```

### Setting up Ceph
In order to use S3, you need to have ceph setup.
To setup ceph run the following command while the s3-server container is running and fully started which takes about 1-2 minutes on my MacBook (it will be running if you do `podman compose up` like stated above).

```sh
podman exec s3-server radosgw-admin user create \
  --uid=admin --display-name="Admin" \
  --access-key=admin --secret-key=password \
  --caps="users=*;buckets=*;usage=*;metadata=*"
```

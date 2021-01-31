## Development
Install dependencies with [nvm](https://github.com/nvm-sh/nvm) and
[yarn](https://yarnpkg.com/).

```
nvm install
nvm use
yarn
```

See [config.example.json](./config.example.json) for the default database
configurations. I reccomend spinning these up with podman or docker. There's
a docker compose file for codespaces in `../.devcontainer` that may be of use
as a reference.

You will also need to generate words.txt if you're using the password
generator. Assuming you have a dictionary installed to
`/usr/share/dict/words`, the following will generate the file for you.

```
cat /usr/share/dict/words | grep -v "'" | awk '{ if (length($0) > 2) print tolower($0)}' | grep -Pv '[^\x00-\x7F]' | uniq > words.txt
```

On ubuntu and debian, I use the wamerican package, though other dictionaries
should work. The grep command will filter out special characters, so if
you're using something outside of basic ascii, you might want to modify that.

Once you have dependencies and config set, run `yarn dev`. The backend will
be built and served at [localhost:8080](https://localhost:8080), and will
automatically be rebuilt when you save files.

Use `yarn lint` and `yarn test` to check your changes. Of interested may be
`yarn lint:fix` and `yarn test:watch`. Note that there is not 100% test
coverage, so you'll need to manualy verify most of your changes.

## Configuration

Configuration is read from disk and from the environment.

### Env vars
- `NODE_ENV`: "production" or "development", override the node_env set by webpack
- `PORT`: Override the default port for serving the api (8080)
- `CONFIG_PATH`: Specify an alternate configfile path

### Configfile
DEaDASS will check first `CONFIG_PATH`, then `./config.json`, then
`./config.example.json` for the configuration file. Please see
[config.example.json](./config.example.json) to see the config fields.

## Configuration

Configuration is read from disk and from the environment.

### Env vars
- `NODE_ENV`: "production" or "development", override the node_env set by webpack
- `PORT`: Override the default port for serving the api (8080)
- `CONFIG_PATH`: Specify an alternate configfile path

### Configfile
DEaDASS will check first `CONFIG_PATH`, then `./config.json`, then `./config.example.json` for the configuration file. Please see [config.example.json](./config.example.json) to see the config fields.

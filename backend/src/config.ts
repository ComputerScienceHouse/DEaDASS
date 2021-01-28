import { existsSync, readFileSync } from "fs";
import { DBServerConfigStanza } from "./db_wrangler";
import { MailConfigStanza } from "./mail";

export type Config = {
  node_env: string;
  mail: MailConfigStanza;
  db_servers: DBServerConfigStanza[]; // database configurations
  config_path: string; // path to config file
  port: number; // Port to serve the UI on
};

/**
 * Update base with all the fields in update
 * @param base the object to update
 * @param update the fields to update
 * @returns a new object with updated fields
 */
function update<T>(base: T, update: Partial<T>): T {
  return { ...base, ...update };
}

declare let process: {
  env: {
    NODE_ENV: string;
    PORT: number;
    CONFIG_PATH: string;
  };
};

function read_config_from_env(): Partial<Config> {
  const config: Partial<Config> = {};
  if (process.env.NODE_ENV) config.node_env = process.env.NODE_ENV;
  if (process.env.PORT) config.port = process.env.PORT;
  if (process.env.CONFIG_PATH) config.config_path = process.env.CONFIG_PATH;
  return config;
}

function read_config_from_file(filepath: string): Partial<Config> {
  return <Partial<Config>>JSON.parse(readFileSync(filepath, "utf8"));
}

/**
 * Get a config from either a user defined config file path, or from the defaults
 * @param filepath User defined config file path
 * @returns the extracted config
 * @throws when no config is found
 */
function pick_config_file(filepath?: string): Partial<Config> {
  if (filepath) {
    if (existsSync(filepath)) {
      return read_config_from_file(filepath);
    } else {
      console.error(
        `Config file '${filepath}' not found, falling back to default configs.`
      );
    }
  }

  if (existsSync("./config.json")) {
    console.log(`Found config file './config.json'`);
    return read_config_from_file("./config.json");
  } else if (existsSync("./config.example.json")) {
    console.log(`Found config file './config.example.json'`);
    return read_config_from_file("./config.example.json");
  } else {
    console.error(
      "No config file found. Please create './config.json', or define CONFIG_PATH."
    );
    throw "Config not found";
  }
}

/**
 * Perform some validation on a given config
 * * Remove duplicate db server definitions
 */
function validate_config(config: Config): void {
  // Check for duplicate db names
  const find_dups = (arr: string[]): string[] =>
    arr.filter((item, index) => arr.indexOf(item) != index);
  const dups: string[] = find_dups(
    config.db_servers.map((dbconfig) => dbconfig.name)
  );
  if (dups.length > 0) {
    new Set<string>(dups).forEach((name) => {
      console.error(
        `Duplicate config stanzas for dbs with name '${name}'. Using last one only.`
      );
      for (let i = dups.filter((val) => val === name).length; i--; i > 0) {
        config.db_servers.splice(
          config.db_servers.findIndex((config) => config.name === name),
          1
        );
      }
    });
  }
}

/**
 * Get configuration from the environment and config files
 * @returns the extracted config
 * @throws if no configuration file can be found
 */
export default function get_config(): Readonly<Config> {
  let config: Config = {
    node_env: "development",
    mail: {
      host: "",
      auth: { user: "", pass: "" },
      port: undefined,
      admin_address: "",
      ui_host: "",
    },
    db_servers: [],
    config_path: "",
    port: 8080,
  };

  // Read from the environment first, in case a config path is defined, then
  // override the file configuration with environment configuration
  const env_config = read_config_from_env();
  config = update(config, pick_config_file(env_config.config_path));
  config = update(config, env_config);

  validate_config(config);

  // Helpful debug printout
  if (config.node_env === "development") console.log(config);

  return <Readonly<Config>>config;
}

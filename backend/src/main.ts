import { exit } from "process";
import { DBWrangler } from "./db_wrangler";
import Mailer from "./mail";
import PasswordGenerator from "./password_generator";
import get_config, { Config } from "./config";
import express = require("express");
import cors = require("cors");

let config: Config;
try {
  config = get_config();
} catch {
  exit();
}

const wrangler: DBWrangler = new DBWrangler(config.db_servers);
wrangler.init().catch(console.error);

const generator: PasswordGenerator = new PasswordGenerator("./words.txt");

// Create Express app
const app = express();

// Configure body parsing
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Configure cors
const corsOptions: cors.CorsOptions = {
  origin: config.cors_origins,
};
app.options("*", cors());
app.use(cors(corsOptions));

app.use((req, res, next) => {
  if (req.accepts("json")) {
    next();
  } else {
    const error = new Error(
      `Not Acceptable: This server only supports json responses.`
    );
    res.status(406);
    next(error);
  }
});

app.get("/health", (_, res, next) => {
  wrangler
    .is_connected()
    .then((map) => {
      const response = {
        database: Object.fromEntries(map),
      };
      // Return 200 iff all servers are connected
      const status = map.map((server) => server.isConnected).every((b) => b)
        ? 200
        : 500;
      res.status(status).json(response);
    })
    .catch(next);
});

app.get("/databases", (_, res, next) => {
  wrangler
    .list_dbs()
    .then((resp) => res.json(resp))
    .catch(next);
});

type PostDatabasesBody = { server: "string"; name: string; username: string };
app.post("/databases", (req, res, next) => {
  const { server, name, username } = <PostDatabasesBody>req.body;
  const password: string = generator.genPassword();
  wrangler
    .create_db(server, name, username, password)
    .then((resp) => res.status(201).json(resp))
    .catch(next);
});

app.get("/databases/:server", (req, res, next) => {
  const { server } = req.params;
  wrangler
    .list_dbs(server)
    .then((resp) => res.json(resp))
    .catch(next);
});

app.get("/databases/:server/:name", (req, res, next) => {
  const { server, name } = req.params;
  wrangler
    .get_db(server, name)
    .then((resp) => res.json(resp))
    .catch(next);
});

app.options("/databases/:server/:name");
app.delete("/databases/:server/:name", (req, res, next) => {
  const { server, name } = req.params;
  wrangler
    .delete_db(server, name)
    .then((resp) => res.status(204).json(resp))
    .catch(next);
});

const test_mail = false;
if (test_mail) {
  const mail: Mailer = new Mailer(config.mail);

  mail
    .request("mom", "Testing request", "not_a_real_db")
    .catch((err) => console.error(err));
  mail.approve("mom", "Testing approve").catch((err) => console.error(err));
  mail.deny("mom", "Testing deny").catch((err) => console.error(err));
}

app.listen(process.env.PORT || 8080);

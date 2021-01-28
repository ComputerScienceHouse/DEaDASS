import Mongo from "./dbs/mongo";
import Mailer from "./mail";
import PasswordGenerator = require("./password_generator");
import express = require("express");
import { DBWrangler } from "./db_wrangler";

declare let process: {
  env: {
    NODE_ENV: string;
    MAIL_HOST: string;
    MAIL_USER: string;
    MAIL_PASSWORD: string;
    MAIL_PORT: string;
    UI_HOST: string;
    MONGO_CONNECT_STRING: string;
    PORT: number;
  };
};

const wrangler: DBWrangler = new DBWrangler(
  new Mongo("mongo", process.env.MONGO_CONNECT_STRING)
);
wrangler.init().catch(console.error);

const generator: PasswordGenerator = new PasswordGenerator("./words.txt");

// Create Express app
const app = express();

// Configure body parsing
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

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

app.get("/health", (_, res) => {
  const response = {
    database: wrangler.is_connected(),
  };
  // Return 200 iff all servers are connected
  const status = response.database
    .map((server) => server.isConnected)
    .every((b) => b)
    ? 200
    : 500;
  res.status(status).json(response);
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

app.delete("/databases/:server/:name", (req, res, next) => {
  const { server, name } = req.params;
  wrangler
    .delete_db(server, name)
    .then((resp) => res.status(204).json(resp))
    .catch(next);
});

const test_mail = false;
if (test_mail) {
  const port: number | undefined =
    (process.env["MAIL_PORT"] && +process.env["MAIL_PORT"]) || undefined;
  const mail: Mailer = new Mailer(
    process.env.MAIL_HOST,
    process.env.MAIL_USER,
    process.env.MAIL_PASSWORD,
    port,
    process.env.UI_HOST,
    "mom@csh.rit.edu"
  );

  mail
    .request("mom", "Testing request", "not_a_real_db")
    .catch((err) => console.error(err));
  mail.approve("mom", "Testing approve").catch((err) => console.error(err));
  mail.deny("mom", "Testing deny").catch((err) => console.error(err));
}

app.listen(process.env.PORT || 8080);

import Mongo from "./dbs/mongo";
import { DBConnection } from "./db_connection";
import Mailer = require("./mail");
import PasswordGenerator = require("./password_generator");
import express = require("express");

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

const mongo: DBConnection = new Mongo(process.env.MONGO_CONNECT_STRING);
mongo.init().catch(console.error);

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
    database: {
      mongo: {
        isConnected: mongo.is_connected(),
      },
    },
  };
  const status = response.database.mongo.isConnected ? 200 : 500;
  res.status(status).json(response);
});

// TODO app.get("/databases")

type PostDatabasesBody = { type: string; name: string; username: string };
app.post("/databases", (req, res, next) => {
  const { type, name, username } = <PostDatabasesBody>req.body;
  const password: string = generator.genPassword();
  switch (type) {
    case "mongo":
      mongo
        .create_db(name, username, password)
        .then((db) =>
          res.status(201).json({
            user: { username: username, password: password },
            db: db,
          })
        )
        .catch(next);
      break;
  }
});

app.get("/databases/:type", (req, res, next) => {
  const { type } = req.params;
  switch (type) {
    case "mongo":
      mongo
        .list_dbs()
        .then((dbs) => Promise.all(dbs.map((db) => mongo.get_db(db))))
        .then((dbs) => res.json(dbs))
        .catch(next);
      break;
  }
});

app.get("/databases/:type/:name", (req, res, next) => {
  const { type, name } = req.params;
  switch (type) {
    case "mongo":
      mongo
        .get_db(name)
        .then((db) => res.json(db))
        .catch(next);
      break;
  }
});

app.delete("/databases/:type/:name", (req, res, next) => {
  const { type, name } = req.params;
  switch (type) {
    case "mongo":
      mongo
        .delete_db(name)
        .then(() => res.status(204).json())
        .catch(next);
      break;
  }
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

const test_mongo = false;
if (test_mongo) {
  mongo
    .init()
    .then(() => mongo.create_db("bar", "bar", generator.genPassword()))
    .then(() => mongo.set_password("bar", "bar", generator.genPassword()))
    .then(() => mongo.delete_db("bar"))
    .then(() => mongo.close())
    .catch(console.error);
}

app.listen(process.env.PORT || 8080);

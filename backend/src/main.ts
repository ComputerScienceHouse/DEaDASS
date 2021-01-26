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

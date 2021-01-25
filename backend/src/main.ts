import Mongo from "./dbs/mongo";
import { DBConnection } from "./db_connection";
import Mailer = require("./mail");
import PasswordGenerator = require("./password_generator");

declare let process: {
  env: {
    NODE_ENV: string;
    MAIL_HOST: string;
    MAIL_USER: string;
    MAIL_PASSWORD: string;
    MAIL_PORT: string;
    UI_HOST: string;
    MONGO_CONNECT_STRING: string;
  };
};

const generator: PasswordGenerator = new PasswordGenerator("./words.txt");

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
  const mongo: DBConnection = new Mongo(process.env.MONGO_CONNECT_STRING);
  mongo
    .init()
    .then(() => mongo.create("bar", "bar", generator.genPassword()))
    .then(() => mongo.set_password("bar", "bar", generator.genPassword()))
    .then(() => mongo.delete("bar"))
    .then(() => mongo.close())
    .catch(console.error);
}

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
  };
};

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

const generator: PasswordGenerator = new PasswordGenerator("./words.txt");
console.log(`Generated password: ${generator.genPassword()}`);

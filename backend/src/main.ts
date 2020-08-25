import PasswordGenerator = require("./password_generator");
const generator: PasswordGenerator = new PasswordGenerator("./words.txt");

console.log(`Generated password: ${generator.genPassword()}`);

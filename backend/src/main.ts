import PasswordGenerator = require("./password_generator");
const generator = new PasswordGenerator("./words.txt");

console.log(`Generated password: ${generator.genPassword()}`);

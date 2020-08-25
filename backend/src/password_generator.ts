import crypto = require("crypto");
import fs = require("fs");

const symbols: string[] = Array.from("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
const min_digits = 4;

function getRandomNumber() {
  return crypto.randomBytes(6).readUInt32BE();
}

class PasswordGenerator {
  private wordlist: string[];
  private max_length: number;
  constructor(
    private wordlist_filepath: string,
    private max_length: number = 32
  ) {
    this.wordlist = fs.readFileSync(wordlist_filepath, "utf8").split("\n");
    this.max_length = max_length;
    this.word_length = 0;
    for (const word: string of this.wordlist) {
      if (word.length > this.word_length) {
        this.word_length = word.length;
      }
    }
  }

  public genPassword() {
    let cur_length = 0;
    let password = "";
    const pass_words: string[] = [];

    // Add a word to the array until we can't be sure we can fit another word,
    // leaving some room for numbers
    // Current length + room for digits + room for symbols < max_length
    while (
      cur_length + pass_words.length * (min_digits + 1) <
      this.max_length
    ) {
      const word: string = this.wordlist[
        getRandomNumber() % this.wordlist.length
      ];

      // No duplicates!
      if (pass_words.includes(word)) {
        continue;
      }

      // Also don't use any words that are too long
      if (
        word.length + cur_length + (pass_words.length - 1) * (1 + min_digits) >
        this.max_length
      ) {
        continue;
      }

      pass_words.push(word);
      cur_length += word.length;
    }

    // Determine how much space is left in the password for digits,
    // leaving space for random symbols between words.
    let length_remaining: number =
      this.max_length - (cur_length + (pass_words.length - 1));

    while (pass_words.length > 1) {
      // Select a word
      password += pass_words.pop();

      // Select a random number
      const random_number: string = (
        getRandomNumber() % Math.pow(10, getRandomNumber() % length_remaining)
      ).toString();
      if (random_number.length <= length_remaining) {
        password += random_number;
        length_remaining -= random_number.length;
      }

      // Select a random symbol
      password += symbols[getRandomNumber() % symbols.length];
    }

    // Fill any remaining space with randomly selected digits
    while (length_remaining > 0) {
      password += getRandomNumber() % 10;
      length_remaining--;
    }

    password += pass_words.pop();

    return password;
  }
}

module.exports = PasswordGenerator;

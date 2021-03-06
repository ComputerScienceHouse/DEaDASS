import { randomBytes } from "crypto";
import { readFileSync } from "fs";

const symbols: string[] = Array.from("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
const min_digits = 4;
const num_symbols = 0;

/**
 * Get a random 32 bit number using the crypto API.
 * @returns A random number
 */
function getRandomNumber(): number {
  return randomBytes(6).readUInt32BE();
}

class PasswordGenerator {
  /** The list of words to be used in passwords. */
  private readonly wordlist: string[];
  /** Try to capitalise this number of characters in generated passwords */
  private readonly caps_attempts: number;

  /**
   * @param wordlist_filepath  path to the wordlist file
   * @param max_length  length of passwords generated by this generator. Default of 32 characters.
   */
  public constructor(
    wordlist_filepath: string,
    private readonly max_length: number = 32
  ) {
    this.wordlist = readFileSync(wordlist_filepath, "utf8").split("\n");
    this.caps_attempts = Math.floor(this.max_length / 4);
  }

  /**
   * Generate a password of length [[max_length]]
   * @return a new password
   */
  public genPassword(): string {
    let cur_length = 0;
    let password = "";
    const pass_words: string[] = [];

    // Add a word to the array until we can't be sure we can fit another word,
    // leaving some room for numbers
    // Current length + room for digits + room for symbols < max_length
    while (
      cur_length + pass_words.length * (min_digits + num_symbols) <
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
        word.length +
          cur_length +
          pass_words.length * (num_symbols + min_digits) >
        this.max_length
      ) {
        // If we don't have at all enough space left, stop getting new words
        if (word.length < min_digits) break;
        continue;
      }

      pass_words.push(word);
      cur_length += word.length;
    }

    // Determine how much space is left in the password for digits,
    // leaving space for random symbols between words.
    let length_remaining: number = this.max_length - cur_length;

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

      // Select random symbols
      for (let i = 0; i < num_symbols; i++) {
        password += symbols[getRandomNumber() % symbols.length];
      }
    }
    // Fill any remaining space with randomly selected digits
    while (length_remaining > 0) {
      password += getRandomNumber() % 10;
      length_remaining--;
    }

    password += pass_words.pop();

    for (let i = 0; i < this.caps_attempts; i++) {
      const index = getRandomNumber() % password.length;
      password =
        password.substr(0, index) +
        password[index].toUpperCase() +
        password.substr(index + 1);
    }

    return password;
  }
}

export default PasswordGenerator;

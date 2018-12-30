package password;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * A 32 charachter password generator.
 * Uses <it>words.txt</it> in project root to create somewhat memorable long passwords.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Password {

    private static final int LENGTH = 32;
    /** The wordlist contains words up to 9 letters long. */
    private static final int WORD_LENGTH = 9;
    private static final int MINIMUM_DIGITS = 2;
    private static final String FILE = "words.txt";
    private static final char[] SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray();
    private static String[] words = null;
    private static SecureRandom rand = null;


    /**
     * Initialises the password generator
     */
    public static void init() {
        if(words != null || rand != null) // Only allow initialising once
            return;
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FILE));
            String line;
            while ((line = reader.readLine()) != null)
                list.add(line);
        } catch (FileNotFoundException e) { // TODO report an error
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        words = list.toArray(new String[list.size()]);

        rand = new SecureRandom();
    }


    /**
     * Generates a random, 32 character password
     * @return the password
     */
    public static String getPassword() {
        StringBuilder password = new StringBuilder();
        ArrayList<String> pass_words = new ArrayList<String>();
        int length = 0;

        // Add words to a list until we aren't sure we could fit another word, plus room for numbers
        while(length + (pass_words.size() - 1) < LENGTH - (WORD_LENGTH + MINIMUM_DIGITS)) {
            String word = words[rand.nextInt(words.length)];
            if(pass_words.contains(word))
                continue;

            pass_words.add(word);
            length += word.length();
        }

        // Determine how much space is left in the string,
        // leaving space for random symbols between words`
        int length_remaining = LENGTH - (length + (pass_words.size() - 1));

        while(pass_words.size() > 1) {
            // Select a word
            password.append(pass_words.remove(pass_words.size() - 1));

            // Select a number that is up to `length_remaining` digits long to fill space.
            String random_number = String.valueOf(rand.nextInt((int) Math.pow(10, rand.nextInt(length_remaining))));
            if(length_remaining > random_number.length()) {
                password.append(random_number);
                length_remaining -= random_number.length();
            }

            // Select a random symbol
            password.append(SYMBOLS[rand.nextInt(SYMBOLS.length)]);
        }

        // Fill any extra remaining space randomly selected digits
        for( int i = 0; i < length_remaining; i++)
            password.append(rand.nextInt(10));

        password.append(pass_words.remove(0));

        return password.toString();
    }
}

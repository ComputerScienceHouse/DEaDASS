package password;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A 32 charachter password generator.
 * Uses <it>words.txt</it> in project root to create somewhat memorable long passwords.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Password {

    private static final int LENGTH = 32;
    private static final String FILE = "words.txt";
    private static final char[] SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray();
    private static String[] words;


    /**
     * Initialises the password generator
     */
    public static void init() {
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
    }


    /**
     * Generates a random, 32 character password
     * @return the password
     */
    public static String getPassword() {
        String password = "";
        ArrayList<String> pass_words = new ArrayList<String>();
        int length = 0;
        while(length + 2 * (pass_words.size() - 1) < LENGTH - 9) {
            String word = words[(int)(Math.random() * words.length)];
            if(pass_words.contains(word))
                continue;

            pass_words.add(word);
            length += word.length();
        }

        int unit = (LENGTH - length) / (pass_words.size() - 1);
        length = LENGTH - length - (pass_words.size() - 1) * unit;

        password += pass_words.remove(pass_words.size() - 1);
        for(int i = 1; i < length + unit; i++)
            password += (int)(Math.random() * 10);
        password += SYMBOLS[(int)(Math.random() * SYMBOLS.length)];

        while(pass_words.size() > 1) {
            password += pass_words.remove(pass_words.size() - 1);
            for(int j = 1; j < unit; j++)
                password += (int)(Math.random() * 10);
            password += SYMBOLS[(int)(Math.random() * SYMBOLS.length)];
        }

        password += pass_words.remove(0);
        return password;
    }
}

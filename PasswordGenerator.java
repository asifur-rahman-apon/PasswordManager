
package f.passwordmanager;

import java.security.SecureRandom;


public class PasswordGenerator {
    private static final SecureRandom rnd = new SecureRandom();
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?/";
    private static final String[] WORDS = { "apple", "orange", "banana", "coffee", "river", "mountain", "sunrise",
            "ocean", "garden", "silver", "purple", "tiger", "lion", "eagle", "forest", "keyboard", "window", "market",
            "paper", "stone" };

    public static String generateChars(int length, boolean letters, boolean digits, boolean symbols) {
        StringBuilder pool = new StringBuilder();
        if (letters) {
            pool.append(LOWER).append(UPPER);
        }
        if (digits)
            pool.append(DIGITS);
        if (symbols)
            pool.append(SYMBOLS);
        if (pool.length() == 0)
            pool.append(LOWER).append(UPPER).append(DIGITS);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++)
            out.append(pool.charAt(rnd.nextInt(pool.length())));
        return out.toString();
    }

    public static String generateWords(int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0)
                out.append('-');
            out.append(WORDS[rnd.nextInt(WORDS.length)]);
        }
        return out.toString();
    }
}

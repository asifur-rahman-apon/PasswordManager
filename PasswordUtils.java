
package f.passwordmanager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;


public class PasswordUtils {
    public static String checkComplexity(String pwd) {
        if (pwd.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        boolean hasLetter = false, hasDigit = false, hasSpecial = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c))
                hasLetter = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSpecial = true;
        }
        if (!hasLetter) {
            return "Password must include letters.";
        }
        if (!hasDigit) {
            return "Password must include digits.";
        }
        if (!hasSpecial) {
            return "Password must include special characters.";
        }
        return null; // Null indicates success
    }

    public static String hashString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(d);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean verifyHash(String s, String expected) {
        return hashString(s).equals(expected);
    }

    public static String generate(int len, boolean words, boolean letters, boolean digits, boolean symbols) {
        if (words)
            return PasswordGenerator.generateWords(len);
        else
            return PasswordGenerator.generateChars(len, letters, digits, symbols);
    }

    public static double estimateStrengthBits(String pwd, boolean isWords) {
        if (pwd == null || pwd.isEmpty())
            return 0.0;
        if (isWords) {
            String[] parts = pwd.split("[-\s]+");
            double bitsPerWord = Math.log(20) / Math.log(2);
            return parts.length * bitsPerWord;
        }
        int pool = 0;
        boolean hasLower = false, hasUpper = false, hasDigits = false, hasSymbols = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isDigit(c))
                hasDigits = true;
            else
                hasSymbols = true;
        }
        if (hasLower)
            pool += 26;
        if (hasUpper)
            pool += 26;
        if (hasDigits)
            pool += 10;
        if (hasSymbols)
            pool += 32;
        if (pool == 0)
            pool = 26;
        double bits = pwd.length() * (Math.log(pool) / Math.log(2));
        return bits;
    }

    public static String strengthLabel(double bits) {
        if (bits < 40)
            return "Weak";
        if (bits < 80)
            return "Moderate";
        return "Strong";
    }
}

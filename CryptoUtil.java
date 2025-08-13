
package f.passwordmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptoUtil {
    private static final SecureRandom random = new SecureRandom();
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String HASH_ALG = "PBKDF2WithHmacSHA256";
    private static final String AES_ALG = "AES/GCM/NoPadding";

    public static byte[] generateSalt() {
        byte[] s = new byte[SALT_BYTES];
        random.nextBytes(s);
        return s;
    }

    public static String hashPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALG);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        byte[] key = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }

    public static boolean verifyPassword(String password, byte[] salt, String expectedHash) throws Exception {
        String h = hashPassword(password, salt);
        return MessageDigest.isEqual(h.getBytes(StandardCharsets.UTF_8), expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    public static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALG);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encryptObject(Object obj, SecretKey key) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        byte[] plain = baos.toByteArray();
        Cipher cipher = Cipher.getInstance(AES_ALG);
        byte[] iv = new byte[12];
        random.nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] cipherText = cipher.doFinal(plain);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(iv);
        out.write(cipherText);
        return out.toByteArray();
    }

    public static Object decryptObject(byte[] data, SecretKey key) throws Exception {
        if (data.length < 12)
            throw new IllegalArgumentException("Ciphertext too short");
        byte[] iv = Arrays.copyOfRange(data, 0, 12);
        byte[] ct = Arrays.copyOfRange(data, 12, data.length);
        Cipher cipher = Cipher.getInstance(AES_ALG);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] plain = cipher.doFinal(ct);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(plain))) {
            return ois.readObject();
        }
    }
}
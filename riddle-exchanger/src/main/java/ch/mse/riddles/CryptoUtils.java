package ch.mse.riddles;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

public class CryptoUtils {
    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;

    public static KeyPair generateKeyPair() {
        SecureRandom random = new SecureRandom();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(KEY_SIZE, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            System.out.println("Error generating key pair");
        }
        return null;
    }

    public static byte[] encrypt(String data, PublicKey pubKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher.doFinal(data.getBytes());
            return result;
        } catch (Exception e) {
            System.out.println("Error encrypting data");
        }
        return null;
    }

    public static byte[] decrypt(byte[] data, PrivateKey privKey) {
        try {

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            byte[] result = cipher.doFinal(data);
            return result;
        } catch (Exception e) {
            System.out.println("Error decrypting data");
        }
        return null;
    }

}
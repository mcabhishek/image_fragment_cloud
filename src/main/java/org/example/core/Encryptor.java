package org.example.core;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class Encryptor {
    private static final String ALGO = "AES/GCM/NoPadding";
    private final SecretKeySpec keySpec;

    public Encryptor(String secret) {
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(), 16);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));
        byte[] cipherText = cipher.doFinal(data);
        byte[] result = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);
        return result;
    }

    public byte[] decrypt(byte[] encryptedData) throws Exception {
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 12);
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 12, encryptedData.length);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));
        return cipher.doFinal(cipherText);
    }
}
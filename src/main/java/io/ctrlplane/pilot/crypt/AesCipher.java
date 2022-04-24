package io.ctrlplane.pilot.crypt;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/** The AES-GCM encryptor. */
@Component
public class AesCipher {

    /** A random generator for IVs. */
    private final SecureRandom random = new SecureRandom();

    /**
     * Encrypts the data.
     *
     * @param data The data to encrypt.
     *
     * @return A result containing cipher and IV.
     *
     * @throws Exception on AES error.
     */
    public CipherResult encrypt(
            final byte[] secretKey,
            final byte[] data)
            throws Exception {
        final SecretKeySpec key =
                new SecretKeySpec(
                        secretKey,
                        "AES");

        final byte[] iv = new byte[16];
        this.random.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(128, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        final byte[] ciphertext = cipher.doFinal(data);
        return new CipherResult(ciphertext, iv);
    }

    /**
     * Decrypts the data.
     *
     * @param iv   The decryption IV.
     * @param data The data to decrypt.
     *
     * @return The decrypted result.
     *
     * @throws Exception on AES error.
     */
    public byte[] decrypt(
            final byte[] secretKey,
            final byte[] iv,
            final byte[] data)
            throws Exception {
        final SecretKeySpec key =
                new SecretKeySpec(
                        secretKey,
                        "AES");
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
        return cipher.doFinal(data);
    }

}

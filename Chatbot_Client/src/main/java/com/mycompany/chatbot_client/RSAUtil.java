package com.mycompany.chatbot_client;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Ho tro tao cap khoa RSA va giai ma khoa AES do Server gui ve.
 */
public final class RSAUtil {

    private static final String RSA_TRANSFORMATION =
            "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int RSA_KEY_SIZE = 2048;

    private RSAUtil() {
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_SIZE);
        return generator.generateKeyPair();
    }

    public static String publicKeyToBase64(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded()
        );
    }

    public static SecretKey decryptAESKey(
            String encryptedKeyBase64,
            PrivateKey privateKey
    ) throws Exception {

        byte[] encryptedKey = Base64.getDecoder().decode(
                encryptedKeyBase64
        );

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] aesKeyBytes = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(aesKeyBytes, "AES");
    }
}

package com.mycompany.chatbot_server;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Ho tro nhan RSA Public Key cua Client, sinh khoa AES theo phien
 * va ma hoa khoa AES truoc khi gui ve Client.
 */
public final class RSAUtil {

    private static final String RSA_TRANSFORMATION =
            "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int AES_KEY_SIZE = 128;

    private RSAUtil() {
    }

    public static PublicKey publicKeyFromBase64(
            String publicKeyBase64
    ) throws Exception {

        byte[] encodedKey = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(AES_KEY_SIZE);
        return generator.generateKey();
    }

    public static String encryptAESKey(
            SecretKey aesKey,
            PublicKey clientPublicKey
    ) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);

        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }
}

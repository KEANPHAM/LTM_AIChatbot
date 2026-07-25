package com.mycompany.chatbot_server;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Ma hoa va giai ma du lieu bang khoa AES cua tung phien ket noi.
 *
 * Khoa AES khong con doc tu config.properties. Server tu sinh khoa AES
 * moi khi Client ket noi va gui khoa do cho Client bang RSA.
 */
public final class AESUtil {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AESUtil() {
    }

    public static String encrypt(String data, SecretKey sessionKey) throws Exception {
        validateKey(sessionKey);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(
                Cipher.ENCRYPT_MODE,
                sessionKey,
                new IvParameterSpec(iv)
        );

        byte[] encryptedBytes = cipher.doFinal(
                data.getBytes(StandardCharsets.UTF_8)
        );

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(
                encryptedBytes,
                0,
                combined,
                iv.length,
                encryptedBytes.length
        );

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(
            String encryptedData,
            SecretKey sessionKey
    ) throws Exception {
        validateKey(sessionKey);

        byte[] combined = Base64.getDecoder().decode(encryptedData);

        if (combined.length <= IV_LENGTH_BYTES) {
            throw new IllegalArgumentException("Du lieu ma hoa khong hop le.");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] encryptedBytes =
                new byte[combined.length - IV_LENGTH_BYTES];

        System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(
                combined,
                IV_LENGTH_BYTES,
                encryptedBytes,
                0,
                encryptedBytes.length
        );

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(
                Cipher.DECRYPT_MODE,
                sessionKey,
                new IvParameterSpec(iv)
        );

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static void validateKey(SecretKey sessionKey) {
        if (sessionKey == null) {
            throw new IllegalStateException(
                    "Chua co khoa AES cua phien ket noi."
            );
        }

        byte[] keyBytes = sessionKey.getEncoded();
        if (keyBytes == null
                || (keyBytes.length != 16
                && keyBytes.length != 24
                && keyBytes.length != 32)) {

            throw new IllegalArgumentException(
                    "Khoa AES phai dai 16, 24 hoac 32 byte."
            );
        }
    }
}

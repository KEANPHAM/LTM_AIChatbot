package com.mycompany.chatbot_client;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

public class AESUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    
    // Yêu cầu 26: Lấy key trực tiếp từ ConfigLoader của Leader, KHÔNG dùng System.getenv() ở đây
    private static byte[] getKeyBytes() {
        // Giả sử ConfigLoader.AES_SECRET_KEY là String. Cần đảm bảo độ dài 16, 24 hoặc 32 bytes
        String secretKey = ConfigLoader.AES_SECRET_KEY; 
        return Arrays.copyOf(secretKey.getBytes(), 16); // Cắt/Pad về 16 bytes (128 bit) cho an toàn
    }

    public static String encrypt(String plainText) throws Exception {
        byte[] clean = plainText.getBytes("UTF-8");
        
        // Sinh ngẫu nhiên 16 bytes IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Ghép IV và CipherText lại: [IV (16 bytes)] + [CipherText]
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decrypt(String encryptedTextBase64) throws Exception {
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedTextBase64);

        // Tách 16 bytes đầu làm IV
        byte[] iv = new byte[16];
        System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Phần còn lại là CipherText
        byte[] encrypted = new byte[encryptedWithIv.length - iv.length];
        System.arraycopy(encryptedWithIv, iv.length, encrypted, 0, encrypted.length);

        SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
}
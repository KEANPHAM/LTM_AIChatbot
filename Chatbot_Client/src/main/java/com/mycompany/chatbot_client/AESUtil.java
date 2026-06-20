package com.mycompany.chatbot_client;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AESUtil - Mã hóa/giải mã AES. (Bản dùng cho Chatbot_Client)
 *
 * THAY ĐỔI SO VỚI BẢN CŨ:
 *  1. Key không còn hardcode "LTMTopic7Chatbot" trong source nữa.
 *     -> Lấy qua ConfigLoader.get(ConfigLoader.AES_SECRET_KEY), đọc từ file
 *        config.properties hoặc biến môi trường (không commit lên Git).
 *  2. Đổi từ "AES" (mặc định = AES/ECB/PKCS5Padding - mode KHÔNG an toàn,
 *     dữ liệu giống nhau sẽ mã hóa ra cùng 1 kết quả, dễ lộ pattern)
 *     sang "AES/CBC/PKCS5Padding" có vector khởi tạo (IV) ngẫu nhiên mỗi
 *     lần mã hóa -> cùng nội dung nhưng mỗi lần mã hóa ra kết quả khác nhau.
 *
 * LƯU Ý QUAN TRỌNG: AES_SECRET_KEY trong config.properties của Client
 * PHẢI GIỐNG Y HỆT (từng ký tự) AES_SECRET_KEY trong config.properties
 * của Server, vì đây là mã hóa đối xứng (symmetric) - hai bên dùng chung
 * 1 key bí mật.
 */
public class AESUtil {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTES = 16; // AES block size = 16 byte

    // Lấy key 1 lần mỗi lúc gọi, không hardcode trong code nữa
    private static SecretKeySpec getKey() {
        String secret = ConfigLoader.get(ConfigLoader.AES_SECRET_KEY);
        // AES key hợp lệ phải đúng 16/24/32 byte (128/192/256-bit).
        // Đồ án dùng key 16 ký tự UTF-8 => AES-128.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES_SECRET_KEY phai dai 16, 24 hoac 32 byte (hien tai: "
                    + keyBytes.length + " byte). Kiem tra lai file config.properties.");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Hàm Mã hóa (Encrypt) - sinh IV ngẫu nhiên, ghép IV vào trước ciphertext
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = getKey();

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Ghép IV + dữ liệu mã hóa thành 1 mảng byte duy nhất để gửi đi,
        // bên nhận sẽ tách IV ra lại trước khi giải mã.
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // Hàm Giải mã (Decrypt) - tách IV ra khỏi phần đầu trước khi giải mã
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = getKey();

        byte[] combined = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] encryptedBytes = new byte[combined.length - IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(combined, IV_LENGTH_BYTES, encryptedBytes, 0, encryptedBytes.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

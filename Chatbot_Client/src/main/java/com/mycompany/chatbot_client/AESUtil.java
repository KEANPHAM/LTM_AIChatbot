package com.mycompany.chatbot_client;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class AESUtil {
    // Chìa khóa bí mật (Secret Key) dài 16 ký tự dùng chung cho cả Client và Server
    private static final String SECRET_KEY = "LTMTopic7Chatbot"; 

    // Hàm Mã hóa (Encrypt)
    public static String encrypt(String data) throws Exception {
        // Bắt buộc ép UTF-8 cho khóa bảo mật
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // Bắt buộc ép UTF-8 cho nội dung tin nhắn
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Hàm Giải mã (Decrypt)
    public static String decrypt(String encryptedData) throws Exception {
        // Bắt buộc ép UTF-8 cho khóa bảo mật
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        // Bắt buộc ép UTF-8 khi chuyển lại thành chuỗi
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
package com.mycompany.chatbot_server;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    
    /**
     * Băm mật khẩu sử dụng thuật toán SHA-256.
     * @param password Mật khẩu gốc (plain-text)
     * @return Chuỗi mã hóa SHA-256 dạng Hex
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Chuyển đổi mảng byte sang chuỗi Hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi hệ thống: Không tìm thấy thuật toán SHA-256", e);
        }
    }
}
package com.mycompany.chatbot_client;

import java.util.regex.Pattern;

public class InputValidator {
    
    public static String validateEmpty(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Lệnh không được để trống!";
        }
        return null;
    }

    public static String validateIP(String ip) {
        String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        if (!Pattern.matches(regex, ip.trim())) {
            return "IP không hợp lệ (phải là định dạng IPv4)!";
        }
        return null;
    }

    public static String validatePort(String xStr, String yStr) {
        try {
            int x = Integer.parseInt(xStr.trim());
            int y = Integer.parseInt(yStr.trim());
            if (x <= 0 || x >= y || y > 65535) {
                return "Port không hợp lệ (Yêu cầu: 0 < x < y ≤ 65535)!";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Port phải là số nguyên dương!";
        }
    }
}
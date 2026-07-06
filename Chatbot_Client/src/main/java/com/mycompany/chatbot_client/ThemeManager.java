package com.mycompany.chatbot_client;

import java.awt.Color;
import java.awt.Font;

public class ThemeManager {
    public static boolean isDarkMode = true; // Mặc định là Dark Mode

    // Font chữ chủ đạo
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 18);

    // Lấy màu nền chính
    public static Color getBgColor() {
        return isDarkMode ? new Color(20, 20, 20) : new Color(250, 250, 250);
    }
    
    // Lấy màu nền cho các Panel/Input nổi (Floating element)
    public static Color getSurfaceColor() {
        return isDarkMode ? new Color(40, 40, 40) : new Color(255, 255, 255);
    }

    // Lấy màu chữ chính
    public static Color getTextColor() {
        return isDarkMode ? new Color(230, 230, 230) : new Color(30, 30, 30);
    }

    // Lấy màu chữ phụ (mờ hơn)
    public static Color getSubTextColor() {
        return isDarkMode ? new Color(150, 150, 150) : new Color(100, 100, 100);
    }

    // Màu nhấn (Accent color) - Xanh lam hiện đại
    public static Color getAccentColor() {
        return new Color(26, 115, 232); 
    }
}
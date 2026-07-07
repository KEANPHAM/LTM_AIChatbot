package com.mycompany.chatbot_client;

import java.awt.Color;
import java.awt.Font;

public class ThemeManager {
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);

    public static Color getBgColor() {
        return new Color(255, 255, 255); // #FFFFFF
    }
    
    public static Color getSurfaceColor() {
        return new Color(248, 249, 250); // #F8F9FA (Card, Floating box)
    }

    public static Color getBorderColor() {
        return new Color(229, 231, 235); // #E5E7EB
    }

    public static Color getAccentColor() {
        return new Color(59, 130, 246); // #3B82F6 (Màu chính)
    }

    public static Color getTextColor() {
        return new Color(17, 24, 39); // #111827 (Text chính)
    }

    public static Color getSubTextColor() {
        return new Color(107, 114, 128); // #6B7280 (Text phụ)
    }
}
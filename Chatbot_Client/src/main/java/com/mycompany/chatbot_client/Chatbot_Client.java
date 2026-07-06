package com.mycompany.chatbot_client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Chatbot_Client {
    public static void main(String[] args) {
        try {
            // Ép Java sử dụng giao diện hiện đại của hệ điều hành (Windows/Mac)
            // để các nút bấm và thanh cuộn nhìn mượt hơn
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Khởi chạy màn hình Đăng nhập (LoginFrame)
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
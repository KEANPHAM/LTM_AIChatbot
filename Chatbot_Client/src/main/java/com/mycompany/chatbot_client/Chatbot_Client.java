package com.mycompany.chatbot_client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Chatbot_Client {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // SỬA CHỖ NÀY: Khởi chạy màn hình nhập IPFrame trước tiên
        SwingUtilities.invokeLater(() -> {
            new IPFrame().setVisible(true);
        });
    }
}
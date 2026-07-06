package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class IPFrame extends JFrame {
    private JTextField txtServerIP;
    private JButton btnConnect;

    
    public IPFrame() {
        setTitle("AI Chatbot - Server Configuration");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(35, 40, 35, 40));
        mainPanel.setBackground(ThemeManager.getBgColor());

        Font font = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblTitle = new JLabel("CẤU HÌNH KẾT NỐI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        txtServerIP = new JTextField();
        styleInput(txtServerIP, font);
        mainPanel.add(createInputWrapper("Địa chỉ IP Server (Mặc định: localhost):", txtServerIP, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        btnConnect = new JButton("TIẾP TỤC");
        btnConnect.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnConnect.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConnect.setBackground(ThemeManager.getAccentColor());
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnConnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        UIAnimator.addSmoothHover(btnConnect, ThemeManager.getAccentColor(), new Color(20, 90, 200));
        // Xử lý sự kiện chuyển tiếp khi bấm nút hoặc nhấn Enter
        btnConnect.addActionListener(e -> handleNext());
        txtServerIP.addActionListener(e -> handleNext());

        mainPanel.add(btnConnect);
        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void handleNext() {
        String ip = txtServerIP.getText().trim();
        // Gán IP vào cấu hình kết nối duy nhất của hệ thống
        ServerConnection.getInstance().setHost(ip);
        
        // Đóng cửa sổ nhập IP hiện tại và kích hoạt màn hình Login
        this.dispose();
        new LoginFrame().setVisible(true);
    }

    private JPanel createInputWrapper(String labelText, JTextField txtField, Font f) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(ThemeManager.getBgColor());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(ThemeManager.getSubTextColor());
        lbl.setFont(f);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txtField, BorderLayout.CENTER);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private void styleInput(JTextField txt, Font f) {
        Border defaultBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getSurfaceColor(), 2),
            BorderFactory.createEmptyBorder(8, 10, 8, 10));
        Border focusBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getAccentColor(), 2),
            BorderFactory.createEmptyBorder(8, 10, 8, 10));

        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setFont(f);
        txt.setBorder(defaultBorder);

        txt.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                txt.setBorder(focusBorder);
                txt.setBackground(ThemeManager.getBgColor()); // Tối đi khi click vào
            }
            @Override public void focusLost(FocusEvent e) {
                txt.setBorder(defaultBorder);
                txt.setBackground(ThemeManager.getSurfaceColor()); // Nổi lên khi nhả ra
            }
        });
    }
}
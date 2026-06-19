package com.mycompany.chatbot_client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    private final Color bgColor = new Color(30, 30, 30);
    private final Color fgColor = new Color(220, 220, 220);
    private final Color inputBgColor = new Color(45, 45, 45);
    private final Color accentColor = new Color(0, 153, 255);
    private final Color hoverColor = new Color(50, 180, 255);
    private final Color pressColor = new Color(0, 120, 200);

    public LoginFrame() {
        setTitle("AI Chatbot - Login");
        setSize(400, 430);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(bgColor);

        Font font = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(accentColor);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Gói ô nhập liệu vào Panel để căn lề trái thẳng tắp
        txtUsername = new JTextField();
        styleInput(txtUsername, font);
        mainPanel.add(createInputWrapper("Username:", txtUsername, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtPassword = new JPasswordField();
        styleInput(txtPassword, font);
        mainPanel.add(createInputWrapper("Password:", txtPassword, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Fix lỗi nút trắng của Windows
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(accentColor);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnLogin.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e) { btnLogin.setBackground(accentColor); }
            @Override public void mousePressed(MouseEvent e) { btnLogin.setBackground(pressColor); }
            @Override public void mouseReleased(MouseEvent e) { btnLogin.setBackground(hoverColor); }
        });

        mainPanel.add(btnLogin);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(255, 85, 85));
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblError);

        JLabel lblRegisterLink = new JLabel("<html><u>Chưa có tài khoản? Tạo mới ngay</u></html>");
        lblRegisterLink.setForeground(new Color(150, 150, 150));
        lblRegisterLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblRegisterLink);

        add(mainPanel);

        lblRegisterLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new RegisterFrame().setVisible(true); }
            @Override public void mouseEntered(MouseEvent e) { lblRegisterLink.setForeground(accentColor); }
            @Override public void mouseExited(MouseEvent e) { lblRegisterLink.setForeground(new Color(150, 150, 150)); }
        });

        btnLogin.addActionListener((ActionEvent e) -> {
            String user = txtUsername.getText();
            boolean loginSuccess = true; 
            if (loginSuccess) {
                this.dispose(); 
                new ChatGUI(user).setVisible(true); 
            } else {
                lblError.setText("⚠ Sai tài khoản hoặc mật khẩu!");
            }
        });
        setLocationRelativeTo(null);
    }

    // Hàm bọc giúp chữ và ô nhập luôn dính sát mép trái
    private JPanel createInputWrapper(String labelText, JTextField txtField, Font f) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(bgColor);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(fgColor);
        lbl.setFont(f);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txtField, BorderLayout.CENTER);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private void styleInput(JTextField txt, Font f) {
        Border defaultBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10));
        Border focusBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10));

        txt.setBackground(inputBgColor);
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setFont(f);
        txt.setBorder(defaultBorder);

        txt.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                txt.setBorder(focusBorder);
                txt.setBackground(new Color(55, 55, 55));
            }
            @Override public void focusLost(FocusEvent e) {
                txt.setBorder(defaultBorder);
                txt.setBackground(inputBgColor);
            }
        });
    }
}

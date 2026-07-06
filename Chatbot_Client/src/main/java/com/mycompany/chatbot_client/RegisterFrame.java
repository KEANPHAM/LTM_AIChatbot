package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblError;

    public RegisterFrame() {
        setTitle("Đăng ký tài khoản");
        setSize(420, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ThemeManager.getBgColor());
        mainPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // HEADER: Chứa nút Quay lại
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JButton btnBack = new JButton("<html><b>&larr;</b> Quay lại</html>");
        btnBack.setFont(ThemeManager.FONT_REGULAR);
        btnBack.setForeground(ThemeManager.getSubTextColor());
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true); // Mở lại form đăng nhập
        });
        headerPanel.add(btnBack);

        // TIÊU ĐỀ
        JLabel lblTitle = new JLabel("Tạo tài khoản mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FORM INPUT
        txtUsername = createFloatingInput();
        txtEmail = createFloatingInput();
        txtPassword = createFloatingPassword();
        txtConfirmPassword = createFloatingPassword();

        // NÚT ĐĂNG KÝ
        JButton btnRegister = new JButton("Đăng ký ngay");
        btnRegister.setFont(ThemeManager.FONT_BOLD);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        UIAnimator.addSmoothHover(btnRegister, ThemeManager.getAccentColor(), new Color(20, 90, 200));

        // RÁP VÀO LAYOUT (Sử dụng RigidArea để tạo Spacing rộng rãi)
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        mainPanel.add(createInputWrapper("Tên đăng nhập:", txtUsername));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createInputWrapper("Địa chỉ Email:", txtEmail));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createInputWrapper("Mật khẩu:", txtPassword));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createInputWrapper("Xác nhận mật khẩu:", txtConfirmPassword));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        mainPanel.add(btnRegister);

        add(mainPanel);
    }

    private JTextField createFloatingInput() {
        JTextField txt = new JTextField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getSurfaceColor(), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return txt;
    }

    private JPasswordField createFloatingPassword() {
        JPasswordField txt = new JPasswordField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getSurfaceColor(), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return txt;
    }

    private JPanel createInputWrapper(String label, JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JLabel lbl = new JLabel(label);
        lbl.setForeground(ThemeManager.getSubTextColor());
        lbl.setFont(ThemeManager.FONT_REGULAR);
        
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txtField, BorderLayout.CENTER);
        return panel;
    }
}
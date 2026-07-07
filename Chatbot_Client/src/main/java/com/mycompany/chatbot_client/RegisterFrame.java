package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

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
    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword, txtConfirmPassword;

    public RegisterFrame() {
        setTitle("Đăng ký tài khoản");
        setSize(420, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(ThemeManager.getBgColor());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JButton btnBack = new JButton("<html><b>&larr;</b> Quay lại</html>");
        btnBack.setFont(ThemeManager.FONT_REGULAR);
        btnBack.setForeground(ThemeManager.getSubTextColor());
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> this.dispose());
        headerPanel.add(btnBack);

        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI");
        lblTitle.setFont(ThemeManager.FONT_BOLD);
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsername = createFloatingInput();
        txtEmail = createFloatingInput();
        txtPassword = createFloatingPassword();
        txtConfirmPassword = createFloatingPassword();

        JButton btnRegister = new JButton("ĐĂNG KÝ");
        btnRegister.setFont(ThemeManager.FONT_BOLD);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(ThemeManager.getAccentColor());
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        UIAnimator.addSmoothHover(btnRegister, ThemeManager.getAccentColor(), ThemeManager.getAccentColor().darker());

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        mainPanel.add(createInputWrapper("Tên đăng nhập:", txtUsername));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        mainPanel.add(createInputWrapper("Email:", txtEmail));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        mainPanel.add(createInputWrapper("Mật khẩu:", txtPassword));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        mainPanel.add(createInputWrapper("Xác nhận mật khẩu:", txtConfirmPassword));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        mainPanel.add(btnRegister);
        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private JTextField createFloatingInput() {
        JTextField txt = new JTextField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return txt;
    }

    private JPasswordField createFloatingPassword() {
        JPasswordField txt = new JPasswordField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return txt;
    }

    private JPanel createInputWrapper(String label, JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
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
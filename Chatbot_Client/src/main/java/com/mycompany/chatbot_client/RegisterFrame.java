package com.mycompany.chatbot_client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername;
    private JTextField txtEmail; // Ô nhập Gmail mới
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblError;

    private final Color bgColor = new Color(30, 30, 30);
    private final Color fgColor = new Color(220, 220, 220);
    private final Color inputBgColor = new Color(45, 45, 45);
    private final Color accentColor = new Color(0, 153, 255);
    private final Color hoverColor = new Color(50, 180, 255);
    private final Color pressColor = new Color(0, 120, 200);

    public RegisterFrame() {
        setTitle("Đăng ký tài khoản hệ thống");
        setSize(400, 620); // Tăng chiều cao lên 620 để đủ chỗ chứa
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 40, 25, 40));
        mainPanel.setBackground(bgColor);

        Font font = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(accentColor);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        txtUsername = new JTextField();
        styleInput(txtUsername, font);
        mainPanel.add(createInputWrapper("Username:", txtUsername, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // THÊM: Ô nhập Gmail
        txtEmail = new JTextField();
        styleInput(txtEmail, font);
        mainPanel.add(createInputWrapper("Gmail:", txtEmail, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        txtPassword = new JPasswordField();
        styleInput(txtPassword, font);
        mainPanel.add(createInputWrapper("Password:", txtPassword, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        txtConfirmPassword = new JPasswordField();
        styleInput(txtConfirmPassword, font);
        mainPanel.add(createInputWrapper("Confirm Password:", txtConfirmPassword, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // NÚT ĐĂNG KÝ TRUYỀN THỐNG
        JButton btnRegister = new JButton("XÁC NHẬN ĐĂNG KÝ");
        btnRegister.setUI(new javax.swing.plaf.basic.BasicButtonUI()); 
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setBackground(accentColor);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegister.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnRegister.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e) { btnRegister.setBackground(accentColor); }
            @Override public void mousePressed(MouseEvent e) { btnRegister.setBackground(pressColor); }
            @Override public void mouseReleased(MouseEvent e) { btnRegister.setBackground(hoverColor); }
        });

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(bgColor);
        btnWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnWrapper.add(btnRegister, BorderLayout.CENTER);
        mainPanel.add(btnWrapper);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Khoảng cách giữa 2 nút

        // THÊM: NÚT ĐĂNG NHẬP GOOGLE
        JButton btnGoogle = new JButton("G  ĐĂNG NHẬP BẰNG GOOGLE");
        btnGoogle.setUI(new javax.swing.plaf.basic.BasicButtonUI()); 
        btnGoogle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGoogle.setBackground(Color.WHITE); // Nền trắng chuẩn Google
        btnGoogle.setForeground(new Color(50, 50, 50)); // Chữ xám đen
        btnGoogle.setFocusPainted(false);
        btnGoogle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoogle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Hiệu ứng hover cho nút Google
        btnGoogle.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnGoogle.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e) { btnGoogle.setBackground(Color.WHITE); }
        });

        // Bấm vào hiện popup tính năng đang phát triển
        btnGoogle.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Tính năng xác thực OAuth 2.0 qua Google đang được phát triển!", "Tính năng mở rộng", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel googleWrapper = new JPanel(new BorderLayout());
        googleWrapper.setBackground(bgColor);
        googleWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        googleWrapper.add(btnGoogle, BorderLayout.CENTER);
        mainPanel.add(googleWrapper);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setForeground(new Color(255, 85, 85)); 
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblError);

        add(mainPanel);

        // Logic xử lý khi bấm đăng ký
        btnRegister.addActionListener((ActionEvent e) -> {
            String user = txtUsername.getText();
            String email = txtEmail.getText();
            String pass = new String(txtPassword.getPassword());
            String confirmPass = new String(txtConfirmPassword.getPassword());

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                lblError.setText("⚠ Vui lòng điền đầy đủ thông tin!");
                return;
            }
            if (!email.endsWith("@gmail.com")) {
                lblError.setText("⚠ Email phải có đuôi @gmail.com!");
                return;
            }
            if (!pass.equals(confirmPass)) {
                lblError.setText("⚠ Mật khẩu không khớp!"); 
                return;
            }
            
            lblError.setForeground(new Color(85, 255, 85));
            lblError.setText("Đang khởi tạo tài khoản...");
            
            // TODO: Ở đây bạn vẫn sẽ chỉ gửi REGISTER|user|pass lên Server thôi nhé
            // để không phá vỡ logic của Hưng và Nhật.
        });
        setLocationRelativeTo(null);
    }

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
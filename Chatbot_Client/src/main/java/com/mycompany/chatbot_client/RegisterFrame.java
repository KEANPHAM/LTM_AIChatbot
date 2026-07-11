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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
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

        // Nút Quay lại
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
            new LoginFrame().setVisible(true); // Quay lại trang đăng nhập
        });
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

        // ==========================================================
        // THÊM SỰ KIỆN XỬ LÝ ĐĂNG KÝ KHI NHẤN NÚT
        // ==========================================================
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            // 1. Kiểm tra đầu vào
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Đổi trạng thái nút để tránh spam click
            btnRegister.setEnabled(false);
            btnRegister.setText("ĐANG XỬ LÝ...");

            // 2. Gửi dữ liệu lên Server trong luồng nền
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    ServerConnection conn = ServerConnection.getInstance();
                    if (!conn.isConnected()) {
                        conn.connect(); // Kết nối nếu chưa kết nối
                    }
                    // Gửi chuỗi: REGISTER|username|password
                    return conn.sendCommand("REGISTER|" + username + "|" + password);
                }

                @Override
                protected void done() {
                    // Phục hồi lại nút
                    btnRegister.setEnabled(true);
                    btnRegister.setText("ĐĂNG KÝ");

                    try {
                        String response = get(); // Nhận kết quả từ doInBackground
                        
                        // 3. Xử lý phản hồi từ Server
                        if (response.equals("REGISTER_RESULT|OK")) {
                            // Hiện thông báo thành công
                            JOptionPane.showMessageDialog(RegisterFrame.this, 
                                "Đăng ký thành công!\nVui lòng đăng nhập để sử dụng.", 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            
                            // Đóng cửa sổ đăng ký và mở lại trang Login
                            dispose();
                            new LoginFrame().setVisible(true);

                        } else if (response.equals("REGISTER_RESULT|FAIL_EXISTS")) {
                            JOptionPane.showMessageDialog(RegisterFrame.this, 
                                "Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.", 
                                "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(RegisterFrame.this, 
                                "Có lỗi xảy ra: " + response, 
                                "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(RegisterFrame.this, 
                            "Không thể kết nối đến máy chủ!", 
                            "Lỗi mạng", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        // Add các component vào Panel chính
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
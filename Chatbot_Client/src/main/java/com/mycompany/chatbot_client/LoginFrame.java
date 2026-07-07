package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Đăng nhập");
        setSize(400, 460); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeManager.getBgColor());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtUsername = createFloatingInput();
        txtPassword = createFloatingPassword();
        
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(ThemeManager.FONT_BOLD);
        btnLogin.setBackground(ThemeManager.getAccentColor());
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        UIAnimator.addSmoothHover(btnLogin, ThemeManager.getAccentColor(), ThemeManager.getAccentColor().darker());

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(239, 68, 68)); // Đỏ hiển thị lỗi rõ ràng
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblRegisterLink = new JLabel("<html><u>Chưa có tài khoản? Tạo mới ngay</u></html>");
        lblRegisterLink.setForeground(ThemeManager.getSubTextColor());
        lblRegisterLink.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterLink.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRegisterLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new RegisterFrame().setVisible(true); }
            @Override public void mouseEntered(MouseEvent e) { lblRegisterLink.setForeground(ThemeManager.getAccentColor()); }
            @Override public void mouseExited(MouseEvent e) { lblRegisterLink.setForeground(ThemeManager.getSubTextColor()); }
        });

        btnLogin.addActionListener(this::handleLogin);
        txtPassword.addActionListener(this::handleLogin);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 35)));
        mainPanel.add(createInputWrapper("Username:", txtUsername));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(createInputWrapper("Password:", txtPassword));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(lblError);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(lblRegisterLink);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void handleLogin(ActionEvent e) {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("⚠ Vui lòng điền đầy đủ tài khoản và mật khẩu.");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setForeground(ThemeManager.getAccentColor());
        lblError.setText("Đang xác thực thông tin...");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    ServerConnection conn = ServerConnection.getInstance();
                    conn.connect(); 
                    return conn.sendCommand("LOGIN|" + user + "|" + pass);
                } catch (Exception ex) {
                    return "ERROR|Không thể kết nối tới máy chủ.";
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    String result = get();
                    if (result.startsWith("LOGIN_RESULT|OK")) {
                        lblError.setText(" ");
                        LoginFrame.this.dispose();
                        // Chuyển tiếp Session an toàn, khởi tạo giao diện dựa trên định danh tài khoản mới
                        new ChatGUI(user).setVisible(true);
                    } else if (result.startsWith("LOGIN_RESULT|FAIL")) {
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText("Sai tài khoản hoặc mật khẩu.");
                    } else {
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText(result.contains("ERROR|") ? result.substring(6) : "Không thể kết nối tới máy chủ.");
                    }
                } catch (Exception ex) {
                    lblError.setForeground(new Color(239, 68, 68));
                    lblError.setText("Không thể kết nối tới máy chủ.");
                }
            }
        };
        worker.execute();
    }

    private JTextField createFloatingInput() {
        JTextField txt = new JTextField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
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
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
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
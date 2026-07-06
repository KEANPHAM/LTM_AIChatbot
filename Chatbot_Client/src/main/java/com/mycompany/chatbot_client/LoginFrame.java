package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private JButton btnLogin;

  
    public LoginFrame() {
        setTitle("AI Chatbot - Login");
        setSize(400, 430); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(ThemeManager.getBgColor());

        Font font = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));


        // Gói ô nhập liệu vào Panel để căn lề trái thẳng tắp (Đây là code cũ của bạn)
        txtUsername = new JTextField();
        styleInput(txtUsername, font);
        mainPanel.add(createInputWrapper("Username:", txtUsername, font));
        
        txtPassword = new JPasswordField();
        styleInput(txtPassword, font);
        mainPanel.add(createInputWrapper("Password:", txtPassword, font));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Fix lỗi nút trắng của Windows
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(ThemeManager.getAccentColor());
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // THAY BẰNG DÒNG NÀY (Để hiệu ứng đổi màu diễn ra mượt mà)
        UIAnimator.addSmoothHover(btnLogin, ThemeManager.getAccentColor(), new Color(20, 90, 200));

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
            @Override public void mouseEntered(MouseEvent e) { lblRegisterLink.setForeground(ThemeManager.getAccentColor()); }
            @Override public void mouseExited(MouseEvent e) { lblRegisterLink.setForeground(new Color(150, 150, 150)); }
        });

        btnLogin.addActionListener(this::handleLogin);
        // Cho phép nhấn Enter ở ô password để đăng nhập luôn, tiện khi test
        txtPassword.addActionListener(this::handleLogin);

        setLocationRelativeTo(null);
    }

    /**
     * SỬA: Không còn giả lập "loginSuccess = true" ở Client nữa.
     * Giờ gửi LOGIN|user|pass THẬT lên Server, qua ServerConnection
     * (mở 1 Socket sống xuyên suốt session, dùng lại cho ChatGUI sau này).
     * Dùng SwingWorker để không làm treo giao diện trong lúc chờ Server.
     */
    private void handleLogin(ActionEvent e) {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập đầy đủ Username và Password!");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setForeground(new Color(255, 85, 85));
        lblError.setText("Đang kết nối tới Server...");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    ServerConnection conn = ServerConnection.getInstance();
                    
                    conn.connect(); // mở Socket (nếu chưa có), giữ sống cho cả session

                    String rawMessage = "LOGIN|" + user + "|" + pass;
                    return conn.sendCommand(rawMessage); // ví dụ trả về "LOGIN_RESULT|OK"

                } catch (java.net.ConnectException ce) {
                    // Sửa nhẹ dòng thông báo lỗi cho gọn
                    return "ERROR|Không thể kết nối tới Server. Vui lòng kiểm tra lại trạng thái Server hoặc IP!";
                } catch (java.net.SocketTimeoutException te) {
                    return "ERROR|Quá thời gian chờ phản hồi từ Server (Timeout).";
                } catch (Exception ex) {
                    return "ERROR|Lỗi mạng hoặc giải mã: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
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
                        new ChatGUI(user).setVisible(true);

                    } else if (result.startsWith("LOGIN_RESULT|FAIL")) {
                        lblError.setText("⚠ Sai tài khoản hoặc mật khẩu!");

                    } else if (result.startsWith("ERROR|")) {
                        lblError.setText("⚠ " + result.substring("ERROR|".length()));

                    } else {
                        lblError.setText("⚠ Phản hồi không hợp lệ từ Server: " + result);
                    }
                } catch (Exception ex) {
                    lblError.setText("⚠ Lỗi xử lý phản hồi: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    // Hàm bọc giúp chữ và ô nhập luôn dính sát mép trái
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
package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.concurrent.ExecutionException;

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

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

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
        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
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

        UIAnimator.addSmoothHover(
                btnRegister,
                ThemeManager.getAccentColor(),
                ThemeManager.getAccentColor().darker()
        );

        btnRegister.addActionListener(e -> register(btnRegister));

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

        mainPanel.add(
                createInputWrapper("Xác nhận mật khẩu:", txtConfirmPassword)
        );
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        mainPanel.add(btnRegister);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void register(JButton btnRegister) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(
                txtConfirmPassword.getPassword()
        );

        if (username.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (username.contains("|") || password.contains("|")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tên đăng nhập và mật khẩu không được chứa ký tự |",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu xác nhận không khớp!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("ĐANG XỬ LÝ...");

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                ServerConnection conn = ServerConnection.getInstance();

              
                /*
                 * Luôn tạo Socket mới cho thao tác đăng ký.
                 *
                 * Mục đích: tránh Socket cũ vẫn báo isConnected() nhưng
                 * tunnel ngrok hoặc phía Server đã ngắt nên lệnh không tới Server.
                 */
                conn.reconnect();

                System.out.println("[REGISTER] Đã kết nối, đang gửi lệnh...");

                String response = conn.sendCommand(
                        "REGISTER|" + username + "|" + password
                );

                if (response == null) {
                    throw new IllegalStateException(
                            "Server khong tra ve du lieu."
                    );
                }

                return response.trim();
            }

            @Override
            protected void done() {
                btnRegister.setEnabled(true);
                btnRegister.setText("ĐĂNG KÝ");

                try {
                    String response = get();

                    if ("REGISTER_RESULT|OK".equals(response)) {
                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Đăng ký thành công!\n"
                                        + "Vui lòng đăng nhập để sử dụng.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        dispose();
                        new LoginFrame().setVisible(true);

                    } else if ("REGISTER_RESULT|FAIL_EXISTS".equals(response)) {
                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Tên đăng nhập đã tồn tại hoặc Server không thể "
                                        + "ghi dữ liệu.\n"
                                        + "Hãy kiểm tra log phía Server.",
                                "Đăng ký thất bại",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } else if ("REGISTER_RESULT|FAIL_FORMAT".equals(response)) {
                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Dữ liệu đăng ký gửi lên Server sai định dạng.",
                                "Đăng ký thất bại",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } else {
                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Server trả về: " + response,
                                "Đăng ký thất bại",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);

                } catch (ExecutionException ex) {
                    Throwable cause =
                            ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause);
                }
            }
        }.execute();
    }

    private void showError(Throwable error) {
        error.printStackTrace();

        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = error.getClass().getSimpleName();
        }

        JOptionPane.showMessageDialog(
                RegisterFrame.this,
                "Đăng ký thất bại:\n" + message,
                "Lỗi kết nối",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JTextField createFloatingInput() {
        JTextField txt = new JTextField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ThemeManager.getBorderColor(),
                                1
                        ),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        );
        return txt;
    }

    private JPasswordField createFloatingPassword() {
        JPasswordField txt = new JPasswordField();
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ThemeManager.getBorderColor(),
                                1
                        ),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        );
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

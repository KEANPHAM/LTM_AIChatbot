package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class IPFrame extends JFrame {
    private final JTextField txtServerHost;
    private final JTextField txtServerPort;
    private final JButton btnConnect;

    public IPFrame() {
        setTitle("Cấu hình kết nối");
        setSize(430, 365);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeManager.getBgColor());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("CẤU HÌNH KẾT NỐI");
        lblTitle.setFont(ThemeManager.FONT_BOLD);
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtServerHost = new JTextField("localhost");
        styleInput(txtServerHost);

        txtServerPort = new JTextField("8888");
        styleInput(txtServerPort);

        btnConnect = new JButton("TIẾP TỤC");
        btnConnect.setFont(ThemeManager.FONT_BOLD);
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setBackground(ThemeManager.getAccentColor());
        btnConnect.setFocusPainted(false);
        btnConnect.setBorderPainted(false);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnConnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);

        UIAnimator.addSmoothHover(
                btnConnect,
                ThemeManager.getAccentColor(),
                ThemeManager.getAccentColor().darker()
        );

        btnConnect.addActionListener(e -> handleNext());
        txtServerHost.addActionListener(e -> txtServerPort.requestFocusInWindow());
        txtServerPort.addActionListener(e -> handleNext());

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 22)));
        mainPanel.add(createInputWrapper(
                "Host Server (localhost / 0.tcp.ap.ngrok.io):",
                txtServerHost
        ));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        mainPanel.add(createInputWrapper(
                "Port (8888 / dùng port ngrok):",
                txtServerPort
        ));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        mainPanel.add(btnConnect);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void handleNext() {
        String host = txtServerHost.getText().trim();
        String portText = txtServerPort.getText().trim();

        if (host.isEmpty()) {
            host = "localhost";
        }

        // Cho phép người dùng lỡ dán cả tiền tố tcp:// vào ô Host.
        if (host.startsWith("tcp://")) {
            host = host.substring("tcp://".length()).trim();
        }

        // Nếu người dùng dán dạng host:port vào ô Host thì tự tách ra.
        int lastColon = host.lastIndexOf(':');
        if (lastColon > 0 && host.indexOf(':') == lastColon) {
            String possiblePort = host.substring(lastColon + 1).trim();
            if (possiblePort.matches("\\d+")) {
                portText = possiblePort;
                host = host.substring(0, lastColon).trim();
            }
        }

        final int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Port phải là số từ 1 đến 65535.",
                    "Port không hợp lệ",
                    JOptionPane.ERROR_MESSAGE
            );
            txtServerPort.requestFocusInWindow();
            return;
        }

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Host Server không được để trống.",
                    "Host không hợp lệ",
                    JOptionPane.ERROR_MESSAGE
            );
            txtServerHost.requestFocusInWindow();
            return;
        }

        ServerConnection.getInstance().setEndpoint(host, port);
        dispose();
        new LoginFrame().setVisible(true);
    }

    private JPanel createInputWrapper(String labelText, JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(ThemeManager.getSubTextColor());
        lbl.setFont(ThemeManager.FONT_REGULAR);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txtField, BorderLayout.CENTER);
        return panel;
    }

    private void styleInput(JTextField txt) {
        txt.setBackground(ThemeManager.getSurfaceColor());
        txt.setForeground(ThemeManager.getTextColor());
        txt.setCaretColor(ThemeManager.getTextColor());
        txt.setFont(ThemeManager.FONT_REGULAR);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
}

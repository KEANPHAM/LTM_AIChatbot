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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class IPFrame extends JFrame {
    private JTextField txtServerIP;
    private JButton btnConnect;

    public IPFrame() {
        setTitle("Cấu hình kết nối");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeManager.getBgColor());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(35, 40, 35, 40));
        mainPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("CẤU HÌNH KẾT NỐI");
        lblTitle.setFont(ThemeManager.FONT_BOLD);
        lblTitle.setForeground(ThemeManager.getTextColor());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtServerIP = new JTextField();
        styleInput(txtServerIP);
        
        btnConnect = new JButton("TIẾP TỤC");
        btnConnect.setFont(ThemeManager.FONT_BOLD);
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setBackground(ThemeManager.getAccentColor());
        btnConnect.setFocusPainted(false);
        btnConnect.setBorderPainted(false);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnConnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        UIAnimator.addSmoothHover(btnConnect, ThemeManager.getAccentColor(), ThemeManager.getAccentColor().darker());
        
        btnConnect.addActionListener(e -> handleNext());
        txtServerIP.addActionListener(e -> handleNext());

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(createInputWrapper("Địa chỉ IP Server (Mặc định: localhost):", txtServerIP));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(btnConnect);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void handleNext() {
        String ip = txtServerIP.getText().trim();
        ServerConnection.getInstance().setHost(ip);
        this.dispose();
        new LoginFrame().setVisible(true);
    }

    private JPanel createInputWrapper(String labelText, JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 
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
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }
}
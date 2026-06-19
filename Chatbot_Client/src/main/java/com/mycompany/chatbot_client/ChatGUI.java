package com.mycompany.chatbot_client;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class ChatGUI extends JFrame {
    private String username;
    
    private JPanel sidebar;
    private JScrollPane scrollPane;
    private JTextField txtInput;
    private JButton btnSend;
    private JLabel lblStatus;

    private JPanel historyContainer; 
    private Map<String, JPanel> chatPanels = new HashMap<>(); 
    private Map<String, JLabel> historyButtons = new HashMap<>(); 
    private String currentChatId = null;
    private int chatCounter = 1;

    private Timer typingTimer;
    private JPanel typingWrapper;
    private JLabel lblTyping;

    private final Color sidebarColor = new Color(28, 32, 40); 
    private final Color mainBgColor = new Color(18, 22, 28);  
    private final Color inputBgColor = new Color(35, 40, 50); 
    private final Color accentColor = new Color(0, 120, 212); 
    private final Color userBubbleColor = new Color(38, 42, 51); 
    private final Color textPrimary = new Color(240, 240, 240);
    private final Color textSecondary = new Color(150, 150, 150);

    public ChatGUI(String username) {
        this.username = (username == null || username.trim().isEmpty()) ? "Guest" : username;
        
        // Bắt buộc Popups phải dùng nền trong suốt được
        JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        
        setTitle("AI Copilot Terminal");
        setSize(950, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(mainBgColor);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(mainBgColor);

        // --- NÚT TOGGLE MŨI TÊN ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(mainBgColor);
        JButton btnToggle = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? textPrimary : textSecondary);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                if (sidebar.isVisible()) {
                    // Mũi tên đóng (hướng trái)
                    g2.drawLine(cx + 4, cy - 6, cx - 4, cy);
                    g2.drawLine(cx - 4, cy, cx + 4, cy + 6);
                } else {
                    // Mũi tên mở (hướng phải)
                    g2.drawLine(cx - 4, cy - 6, cx + 4, cy);
                    g2.drawLine(cx + 4, cy, cx - 4, cy + 6);
                }
                g2.dispose();
            }
        };
        btnToggle.setPreferredSize(new Dimension(40, 40));
        btnToggle.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnToggle.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            btnToggle.repaint();
        });
        
        topBar.add(btnToggle);
        mainArea.add(topBar, BorderLayout.NORTH);

        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        scrollPane.getViewport().setBackground(mainBgColor);
        mainArea.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(mainBgColor);
        bottomWrapper.setBorder(new EmptyBorder(10, 60, 30, 60)); 

        RoundedPanel inputPanel = new RoundedPanel(25, inputBgColor);
        inputPanel.setLayout(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(5, 15, 5, 5));

        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtInput.setBackground(inputBgColor);
        txtInput.setForeground(textPrimary);
        txtInput.setCaretColor(textPrimary);
        txtInput.setBorder(null); 
        txtInput.setOpaque(false); 

        btnSend = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0, 90, 180));
                else if (getModel().isRollover()) g2.setColor(new Color(20, 140, 230));
                else g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(Color.WHITE);
                int[] xPoints = {15, 27, 15}; 
                int[] yPoints = {12, 20, 28}; 
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        btnSend.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(45, 40));
        btnSend.setBorder(null);
        btnSend.setContentAreaFilled(false);

        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);

        lblStatus = new JLabel(" Sẵn sàng.");
        lblStatus.setForeground(textSecondary);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setBorder(new EmptyBorder(5, 10, 0, 0));

        bottomWrapper.add(inputPanel, BorderLayout.CENTER);
        bottomWrapper.add(lblStatus, BorderLayout.SOUTH);
        
        mainArea.add(bottomWrapper, BorderLayout.SOUTH);
        add(mainArea, BorderLayout.CENTER);

        btnSend.addActionListener(this::handleSendMessage);
        txtInput.addActionListener(this::handleSendMessage);
        setLocationRelativeTo(null);

        createNewChat();
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(sidebarColor);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblLogo = new JLabel("  Copilot");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(textPrimary);
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblLogo);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel btnNewChat = new JLabel("+   New chat");
        btnNewChat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNewChat.setForeground(textPrimary);
        btnNewChat.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnNewChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewChat.setBorder(new EmptyBorder(8, 10, 8, 10));
        btnNewChat.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnNewChat.setForeground(accentColor); }
            @Override public void mouseExited(MouseEvent e) { btnNewChat.setForeground(textPrimary); }
            @Override public void mouseClicked(MouseEvent e) { createNewChat(); }
        });
        panel.add(btnNewChat);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblRecent = new JLabel("Recent");
        lblRecent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRecent.setForeground(textSecondary);
        panel.add(lblRecent);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setBackground(sidebarColor);

        JScrollPane historyScroll = new JScrollPane(historyContainer);
        historyScroll.setBorder(null);
        historyScroll.setBackground(sidebarColor);
        historyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        historyScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 
        panel.add(historyScroll);

        panel.add(Box.createVerticalGlue()); 
        
        // --- POPUP MENU ĐÃ ĐƯỢC CẮT GỌN CHỈ CÒN ĐĂNG XUẤT ---
        String capName = this.username.substring(0,1).toUpperCase() + this.username.substring(1).toLowerCase();
        JLabel lblFooter = new JLabel("D   " + capName);
        lblFooter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFooter.setForeground(textSecondary);
        lblFooter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPopupMenu userMenu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(inputBgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        userMenu.setOpaque(false);
        userMenu.setBorder(new EmptyBorder(10, 0, 10, 0));
        userMenu.setLayout(new BoxLayout(userMenu, BoxLayout.Y_AXIS));

        // Header Popup
        JPanel headerPop = new JPanel(new GridLayout(2, 1));
        headerPop.setOpaque(false);
        headerPop.setBorder(new EmptyBorder(5, 20, 10, 20));
        JLabel nameLbl = new JLabel(capName);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel emailLbl = new JLabel(this.username.toLowerCase() + "123@gmail.com");
        emailLbl.setForeground(textSecondary);
        emailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        headerPop.add(nameLbl);
        headerPop.add(emailLbl);
        userMenu.add(headerPop);

        // Kẻ ngang
        JPanel sep1 = new JPanel();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep1.setBackground(new Color(60, 60, 60));
        userMenu.add(sep1);
        userMenu.add(Box.createRigidArea(new Dimension(0, 5)));

        // Nút Đăng xuất (Đã bỏ 4 menu con)
        JLabel signoutItem = new JLabel("Sign out");
        signoutItem.setForeground(Color.WHITE);
        signoutItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signoutItem.setBorder(new EmptyBorder(8, 20, 8, 20));
        signoutItem.setOpaque(false);
        signoutItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        signoutItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signoutItem.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { signoutItem.setOpaque(true); signoutItem.setBackground(new Color(200, 50, 50)); signoutItem.repaint(); }
            @Override public void mouseExited(MouseEvent e) { signoutItem.setOpaque(false); signoutItem.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                userMenu.setVisible(false);
                dispose(); 
                new LoginFrame().setVisible(true); 
            }
        });
        userMenu.add(signoutItem);

        // Click để hiển thị Menu
        lblFooter.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                userMenu.show(lblFooter, 0, -userMenu.getPreferredSize().height);
            }
            @Override public void mouseEntered(MouseEvent e) { lblFooter.setForeground(textPrimary); }
            @Override public void mouseExited(MouseEvent e) { lblFooter.setForeground(textSecondary); }
        });
        
        panel.add(lblFooter);

        return panel;
    }

    private void createNewChat() {
        if (currentChatId != null && chatPanels.get(currentChatId).getComponentCount() <= 1) {
            return; 
        }

        String newId = "ChatSession_" + chatCounter++;
        
        JPanel newChatPanel = new JPanel();
        newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
        newChatPanel.setBackground(mainBgColor);
        newChatPanel.setBorder(new EmptyBorder(10, 50, 10, 50)); 

        String displayName = this.username.substring(0, 1).toUpperCase() + this.username.substring(1).toLowerCase();
        JLabel lblHeader = new JLabel("Hi " + displayName + ", what should we dive into today?");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(textPrimary);
        
        JPanel headerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(50, 0, 50, 0)); 
        headerWrapper.add(lblHeader);
        newChatPanel.add(headerWrapper);

        chatPanels.put(newId, newChatPanel);
        currentChatId = newId;

        JPanel outerChatPanel = new JPanel(new BorderLayout());
        outerChatPanel.setBackground(mainBgColor);
        outerChatPanel.add(newChatPanel, BorderLayout.NORTH);
        scrollPane.setViewportView(outerChatPanel);

        addHistoryButton(newId);
    }

    private void addHistoryButton(String chatId) {
        JLabel btn = new JLabel("💬  New Chat"); 
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(textSecondary);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 10, 8, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(textPrimary); }
            @Override public void mouseExited(MouseEvent e) { btn.setForeground(textSecondary); }
            @Override public void mouseClicked(MouseEvent e) {
                currentChatId = chatId;
                JPanel outerChatPanel = new JPanel(new BorderLayout());
                outerChatPanel.setBackground(mainBgColor);
                outerChatPanel.add(chatPanels.get(chatId), BorderLayout.NORTH);
                scrollPane.setViewportView(outerChatPanel);
            }
        });

        historyButtons.put(chatId, btn);
        historyContainer.add(btn, 0); 
        historyContainer.revalidate();
        historyContainer.repaint();
    }

    private void showTypingIndicator() {
        typingWrapper = new JPanel(new BorderLayout());
        typingWrapper.setOpaque(false);
        typingWrapper.setBorder(new EmptyBorder(10, 5, 20, 5)); 

        lblTyping = new JLabel(" ");
        lblTyping.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTyping.setForeground(accentColor); 
        lblTyping.setBorder(new EmptyBorder(0, 10, 0, 0)); 

        JPanel alignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(lblTyping);

        typingWrapper.add(alignPanel, BorderLayout.CENTER);
        
        JPanel currentPanel = chatPanels.get(currentChatId);
        currentPanel.add(typingWrapper);
        currentPanel.revalidate();
        currentPanel.repaint();

        scrollToBottom();

        typingTimer = new Timer(350, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                count = (count + 1) % 4;
                switch (count) {
                    case 0: lblTyping.setText(" "); break;
                    case 1: lblTyping.setText("●"); break;
                    case 2: lblTyping.setText("● ●"); break;
                    case 3: lblTyping.setText("● ● ●"); break;
                }
            }
        });
        typingTimer.start();
    }

    private void hideTypingIndicator() {
        if (typingTimer != null && typingTimer.isRunning()) {
            typingTimer.stop();
        }
        if (typingWrapper != null) {
            JPanel currentPanel = chatPanels.get(currentChatId);
            if (currentPanel != null) {
                currentPanel.remove(typingWrapper);
                currentPanel.revalidate();
                currentPanel.repaint();
            }
            typingWrapper = null;
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void handleSendMessage(ActionEvent e) {
        String command = txtInput.getText();
        String emptyError = InputValidator.validateEmpty(command);
        if (emptyError != null) {
            appendErrorBubble(emptyError);
            return;
        }

        btnSend.setEnabled(false);
        txtInput.setEnabled(false);
        lblStatus.setText(" Đang gửi và chờ AI phân tích...");
        lblStatus.setForeground(accentColor); 

        appendMessage(command, true, false);
        showTypingIndicator();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try (Socket socket = new Socket("localhost", 8888)) {
                    socket.setSoTimeout(300000); 
                    
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    String cmd = command.toUpperCase();

                    String rawMessage;

                    if (cmd.startsWith("WEATHER|")) {
                        rawMessage = "WEATHER|" + username + "|" + command.substring(8);
                    }
                    else if (cmd.startsWith("IP|")) {
                        rawMessage = "IP|" + username + "|" + command.substring(3);
                    }
                    else if (cmd.startsWith("PORT|")) {
                        rawMessage = "PORT|" + username + "|" + command.substring(5);
                    }
                    else {
                        rawMessage = "CHAT|" + username + "|" + command;
                    }
                    
                    String encryptedMessage = AESUtil.encrypt(rawMessage);
                    out.writeUTF(encryptedMessage);

                    String encryptedResponse = in.readUTF();
                    return AESUtil.decrypt(encryptedResponse);

                } catch (java.net.ConnectException ce) {
                    return "ERROR: Không thể kết nối. Server  chưa bật hoặc sai Port!";
                } catch (java.net.SocketTimeoutException te) {
                    return "ERROR: Quá 5 phút không nhận được phản hồi (Timeout).";
                } catch (Exception ex) {
                    return "ERROR: Lỗi mạng hoặc giải mã: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                hideTypingIndicator();

                btnSend.setEnabled(true);
                txtInput.setEnabled(true);
                lblStatus.setText(" Sẵn sàng.");
                lblStatus.setForeground(textSecondary);
                txtInput.setText("");
                txtInput.requestFocus();

                try {
                    String response = get();
                    if (response.startsWith("ERROR:")) {
                        appendErrorBubble(response.replace("ERROR:", ""));
                    } else {
                        appendMessage(response, false, false);
                    }
                } catch (Exception ex) {
                    appendErrorBubble("Lỗi phân luồng dữ liệu.");
                }
            }
        };
        
        worker.execute();
    }

    private void appendErrorBubble(String errorMsg) {
        appendMessage("⚠ " + errorMsg, false, true);
    }

    private void appendMessage(String msg, boolean isUser, boolean isSystem) {
        JPanel currentPanel = chatPanels.get(currentChatId); 

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 5, 20, 5)); 

        JTextArea text = new JTextArea(msg);
        text.setFont(new Font("Segoe UI", isUser ? Font.PLAIN : Font.BOLD, 15)); 
        text.setForeground(isSystem ? new Color(255, 120, 120) : textPrimary);
        text.setOpaque(false);
        text.setEditable(false);
        text.setLineWrap(false);
        text.setWrapStyleWord(false);
        
        if (text.getPreferredSize().width > 500) {
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setSize(new Dimension(500, Short.MAX_VALUE));
            text.setPreferredSize(new Dimension(500, text.getPreferredSize().height));
        }

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isUser) { 
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(userBubbleColor); 
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); 
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(isUser ? 12 : 5, isUser ? 18 : 10, isUser ? 12 : 5, isUser ? 18 : 10)); 
        bubble.add(text, BorderLayout.CENTER);

        JPanel alignPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubble);

        wrapper.add(alignPanel, BorderLayout.CENTER);
        currentPanel.add(wrapper);
        currentPanel.revalidate();
        currentPanel.repaint();

        if (isUser && currentPanel.getComponentCount() == 2) {
            String title = msg.length() > 18 ? msg.substring(0, 18) + "..." : msg;
            JLabel historyBtn = historyButtons.get(currentChatId);
            if (historyBtn != null) {
                historyBtn.setText("💬  " + title);
            }
        }

        scrollToBottom();
    }

    class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color bgColor;

        public RoundedPanel(int radius, Color color) {
            super();
            this.cornerRadius = radius;
            this.bgColor = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
    }
}
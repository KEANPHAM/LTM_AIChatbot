package com.mycompany.chatbot_client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ChatGUI extends JFrame {
    private String username;
    private JPanel sidebar, historyContainer, mainArea;
    private JScrollPane scrollPane;
    private JTextField txtInput;
    private JButton btnSend;
    private JLabel lblStatus;

    private Map<String, JPanel> chatPanels = new HashMap<>();
    private Map<String, ChatHistoryItem> historyItems = new HashMap<>();
    private String currentChatId = null;
    private int chatCounter = 1;

    private Timer typingTimer;
    private JPanel typingWrapper;
    private JLabel lblTyping;

    // ===== Bảng màu hiện đại (Modern palette) =====
    private static final Color BG_COLOR = new Color(0xFFFFFF);
    private static final Color SURFACE_COLOR = new Color(0xF6F7FB);
    private static final Color BORDER_COLOR = new Color(0xE7E8F0);
    private static final Color PRIMARY_COLOR = new Color(0x5B5FEF);
    private static final Color HOVER_COLOR = new Color(0xEEF0FF);
    private static final Color TEXT_COLOR = new Color(0x161B22);
    private static final Color SUB_TEXT_COLOR = new Color(0x8B8FA3);
    private static final Color USER_BUBBLE_COLOR = new Color(0x5B5FEF);
    private static final Color AI_BUBBLE_COLOR = new Color(0xF3F4F8);
    private static final Color ONLINE_COLOR = new Color(0x22C55E);
    private static final Color DANGER_COLOR = new Color(0xEF4444);
    private static final Color DANGER_BG_COLOR = new Color(0xFEF1F2);

    // ===== Font dùng chung =====
    private static final Font FONT_LOGO = new Font("Segoe UI", Font.BOLD, 21);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // ===== Bo góc dùng chung =====
    private static final int RADIUS_LG = 22;
    private static final int RADIUS_MD = 16;
    private static final int RADIUS_SM = 10;

    public ChatGUI(String username) {
        this.username = (username == null || username.trim().isEmpty()) ? "Guest" : username;

        setTitle("AI Chatbot");
        setSize(1150, 760);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        setupSidebar();
        setupMainArea();
        setLocationRelativeTo(null);

        loadChatHistoryFromServer();
    }

    private void setupSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SURFACE_COLOR);
        sidebar.setPreferredSize(new Dimension(272, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SURFACE_COLOR);
        headerPanel.setBorder(new EmptyBorder(26, 22, 18, 22));

        JLabel lblLogo = new JLabel("AI Chatbot"); // Bỏ icon ✦
        lblLogo.setFont(FONT_LOGO);
        lblLogo.setForeground(TEXT_COLOR);
        headerPanel.add(lblLogo, BorderLayout.WEST);
        sidebar.add(headerPanel);

        // New Chat Button (Đã thu nhỏ)
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        buttonWrapper.setBackground(SURFACE_COLOR);
        buttonWrapper.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnNewChat = new RoundedButton("New Chat", RADIUS_SM); // Bỏ icon ＋
        btnNewChat.setFont(FONT_BODY_BOLD);
        btnNewChat.setForeground(Color.WHITE);
        btnNewChat.setBackground(PRIMARY_COLOR);
        btnNewChat.setFocusPainted(false);
        btnNewChat.setBorderPainted(false);
        btnNewChat.setContentAreaFilled(false);
        btnNewChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewChat.setPreferredSize(new Dimension(120, 38)); // Thu nhỏ nút
        
        // Hover effect
        btnNewChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnNewChat.setBackground(PRIMARY_COLOR.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnNewChat.setBackground(PRIMARY_COLOR);
            }
        });

        btnNewChat.addActionListener(e -> createNewChat());
        buttonWrapper.add(btnNewChat);
        sidebar.add(buttonWrapper);

        // History Title
        JPanel historyTitlePanel = new JPanel(new BorderLayout());
        historyTitlePanel.setBackground(SURFACE_COLOR);
        historyTitlePanel.setBorder(new EmptyBorder(4, 24, 10, 24));

        JLabel lblHistory = new JLabel("CHAT HISTORY");
        lblHistory.setFont(FONT_SECTION);
        lblHistory.setForeground(SUB_TEXT_COLOR);
        historyTitlePanel.add(lblHistory, BorderLayout.WEST);
        sidebar.add(historyTitlePanel);

        // History Container
        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setBackground(SURFACE_COLOR);
        historyContainer.setBorder(new EmptyBorder(0, 8, 0, 8));

        JScrollPane historyScroll = new JScrollPane(historyContainer);
        historyScroll.setBorder(null);
        historyScroll.setOpaque(false);
        historyScroll.getViewport().setOpaque(false);
        historyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        historyScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        historyScroll.getVerticalScrollBar().setOpaque(false);
        sidebar.add(historyScroll);

        sidebar.add(Box.createVerticalGlue());

        // User Card
        JPanel userCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_MD, RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userCard.setOpaque(false);
        userCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);

        // Avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_COLOR);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String initial = username.substring(0, 1).toUpperCase();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = (40 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(40, 40));
        avatarPanel.setOpaque(false);
        userCard.add(avatarPanel, gbc);

        // User Info
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);

        JLabel lblUserName = new JLabel(username);
        lblUserName.setFont(FONT_BODY_BOLD);
        lblUserName.setForeground(TEXT_COLOR);
        userInfoPanel.add(lblUserName);

        JLabel lblUserStatus = new JLabel("Online"); // Bỏ icon ●
        lblUserStatus.setFont(FONT_SMALL);
        lblUserStatus.setForeground(ONLINE_COLOR);
        userInfoPanel.add(lblUserStatus);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        userCard.add(userInfoPanel, gbc);

        // Logout Button
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 4, 0, 0);
        JButton btnLogout = new RoundedButton("Logout", 6); // Đổi ✕ thành "Logout"
        btnLogout.setFont(FONT_SMALL);
        btnLogout.setForeground(SUB_TEXT_COLOR);
        btnLogout.setBackground(SURFACE_COLOR);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(65, 30));
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(DANGER_BG_COLOR);
                btnLogout.setForeground(DANGER_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(SURFACE_COLOR);
                btnLogout.setForeground(SUB_TEXT_COLOR);
            }
        });
        btnLogout.addActionListener(e -> {
            ServerConnection.getInstance().disconnect();
            chatPanels.clear();
            historyItems.clear();
            dispose();
            new LoginFrame().setVisible(true);
        });
        userCard.add(btnLogout, gbc);

        JPanel userCardWrapper = new JPanel(new BorderLayout());
        userCardWrapper.setBackground(SURFACE_COLOR);
        userCardWrapper.setBorder(new EmptyBorder(14, 14, 20, 14));
        userCardWrapper.add(userCard, BorderLayout.CENTER);
        sidebar.add(userCardWrapper);

        add(sidebar, BorderLayout.WEST);
    }

    private void setupMainArea() {
        mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BG_COLOR);

        // Thêm thanh công cụ phía trên để chứa nút Toggle
       // Thêm thanh công cụ phía trên để chứa nút Toggle
        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topHeader.setBackground(BG_COLOR);
        
        // Sử dụng biểu tượng ☰ (Hamburger menu)
        JButton btnToggleSidebar = new RoundedButton("->", RADIUS_SM);
        btnToggleSidebar.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Tăng size font để biểu tượng to và rõ hơn
        btnToggleSidebar.setForeground(SUB_TEXT_COLOR);
        btnToggleSidebar.setBackground(SURFACE_COLOR);
        btnToggleSidebar.setFocusPainted(false);
        btnToggleSidebar.setBorderPainted(false);
        btnToggleSidebar.setContentAreaFilled(false);
        btnToggleSidebar.setPreferredSize(new Dimension(45, 32)); // Thu nhỏ chiều ngang vì chỉ còn biểu tượng
        btnToggleSidebar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnToggleSidebar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnToggleSidebar.setBackground(HOVER_COLOR);
                btnToggleSidebar.setForeground(TEXT_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnToggleSidebar.setBackground(SURFACE_COLOR);
                btnToggleSidebar.setForeground(SUB_TEXT_COLOR);
            }
        });
        btnToggleSidebar.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            revalidate();
            repaint();
        });
        topHeader.add(btnToggleSidebar);
        mainArea.add(topHeader, BorderLayout.NORTH);

        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // Custom ScrollBar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);

        mainArea.add(scrollPane, BorderLayout.CENTER);

        // Input Area
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(BG_COLOR);
        bottomWrapper.setBorder(new EmptyBorder(14, 72, 28, 72));

        JPanel inputContainer = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_LG, RADIUS_LG);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_LG, RADIUS_LG);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputContainer.setOpaque(false);
        inputContainer.setBorder(new EmptyBorder(4, 22, 4, 6));
        inputContainer.setPreferredSize(new Dimension(0, 58));

        txtInput = new JTextField();
        txtInput.setFont(FONT_BODY);
        txtInput.setForeground(TEXT_COLOR);
        txtInput.setCaretColor(PRIMARY_COLOR);
        txtInput.setOpaque(false);
        txtInput.setBorder(null);
        txtInput.addActionListener(e -> handleSendMessage());

        btnSend = new RoundedButton("Send", RADIUS_SM);
        btnSend.setFont(FONT_BODY_BOLD);
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(PRIMARY_COLOR);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false);
        btnSend.setContentAreaFilled(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(86, 44));

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSend.setBackground(PRIMARY_COLOR.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnSend.setBackground(PRIMARY_COLOR);
            }
        });

        btnSend.addActionListener(e -> handleSendMessage());

        inputContainer.add(txtInput, BorderLayout.CENTER);
        inputContainer.add(btnSend, BorderLayout.EAST);
        bottomWrapper.add(inputContainer, BorderLayout.CENTER);

        lblStatus = new JLabel("Online"); // Bỏ icon ●
        lblStatus.setForeground(ONLINE_COLOR);
        lblStatus.setFont(FONT_SMALL);
        lblStatus.setBorder(new EmptyBorder(10, 6, 0, 0));
        bottomWrapper.add(lblStatus, BorderLayout.SOUTH);

        mainArea.add(bottomWrapper, BorderLayout.SOUTH);
        add(mainArea, BorderLayout.CENTER);
    }

    private void createNewChat() {
        if (currentChatId != null && chatPanels.get(currentChatId).getComponentCount() <= 0) return;

        String newId = "ChatSession_" + chatCounter++;
        JPanel newChatPanel = new JPanel();
        newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
        newChatPanel.setBackground(BG_COLOR);
        newChatPanel.setBorder(new EmptyBorder(32, 84, 32, 84));

        chatPanels.put(newId, newChatPanel);
        currentChatId = newId;

        JPanel alignPanel = new JPanel(new BorderLayout());
        alignPanel.setBackground(BG_COLOR);
        alignPanel.add(newChatPanel, BorderLayout.NORTH);
        scrollPane.setViewportView(alignPanel);

        addHistoryItem(newId, "New Chat");
    }

    private void addHistoryItem(String chatId, String title) {
        ChatHistoryItem item = new ChatHistoryItem(chatId, title);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectHistoryItem(chatId);
            }
        });

        historyItems.put(chatId, item);
        historyContainer.add(item);
        historyContainer.add(Box.createVerticalStrut(2));
        historyContainer.revalidate();
        historyContainer.repaint();

        // Select if first item
        if (historyItems.size() == 1) {
            selectHistoryItem(chatId);
        }
    }

    private void selectHistoryItem(String chatId) {
        // Deselect all
        for (ChatHistoryItem item : historyItems.values()) {
            item.setSelected(false);
        }

        // Select target
        ChatHistoryItem selected = historyItems.get(chatId);
        if (selected != null) {
            selected.setSelected(true);
            currentChatId = chatId;

            JPanel alignPanel = new JPanel(new BorderLayout());
            alignPanel.setBackground(BG_COLOR);
            alignPanel.add(chatPanels.get(chatId), BorderLayout.NORTH);
            scrollPane.setViewportView(alignPanel);
        }
    }

    private void handleSendMessage() {
        String command = txtInput.getText().trim();
        if (command.isEmpty()) return;

        btnSend.setEnabled(false);
        txtInput.setEnabled(false);
        lblStatus.setText("Typing..."); // Bỏ icon ●
        lblStatus.setForeground(SUB_TEXT_COLOR);

        txtInput.setText("");
        appendMessage(currentChatId, command, true);
        showTypingIndicator();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                ServerConnection conn = ServerConnection.getInstance();
                if (!conn.isConnected()) return "ERROR|Connection lost.";
                String rawMessage = "CHAT|" + username + "|" + currentChatId + "|" + command;
                return conn.sendCommand(rawMessage);
            }

            @Override
            protected void done() {
                hideTypingIndicator();
                btnSend.setEnabled(true);
                txtInput.setEnabled(true);
                lblStatus.setText("Online"); // Bỏ icon ●
                lblStatus.setForeground(ONLINE_COLOR);
                txtInput.requestFocus();

                try {
                    String response = get();
                    if (response.startsWith("ERROR|")) {
                        appendMessage(currentChatId, "[Error] " + response.substring(6), false); // Bỏ icon ⚠
                    } else {
                        appendMessage(currentChatId, response, false);
                    }
                } catch (Exception ex) {
                    appendMessage(currentChatId, "[Error] Failed to receive response.", false);
                }
            }
        };
        worker.execute();
    }

    private void showTypingIndicator() {
        JPanel currentPanel = chatPanels.get(currentChatId);
        if (currentPanel == null) return;

        typingWrapper = new JPanel(new BorderLayout());
        typingWrapper.setOpaque(false);
        typingWrapper.setBorder(new EmptyBorder(8, 0, 12, 0));

        lblTyping = new JLabel(" ");
        lblTyping.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTyping.setForeground(SUB_TEXT_COLOR);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AI_BUBBLE_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_MD, RADIUS_MD);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 20, 10, 20));
        bubble.add(lblTyping, BorderLayout.CENTER);

        JPanel alignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubble);

        typingWrapper.add(alignPanel, BorderLayout.CENTER);
        currentPanel.add(typingWrapper);
        currentPanel.revalidate();
        currentPanel.repaint();
        scrollToBottom();

        typingTimer = new Timer(350, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                count = (count + 1) % 4;
                switch (count) { // Thay ● bằng dấu chấm tiêu chuẩn .
                    case 0: lblTyping.setText(" "); break;
                    case 1: lblTyping.setText("."); break;
                    case 2: lblTyping.setText(".."); break;
                    case 3: lblTyping.setText("..."); break;
                }
            }
        });
        typingTimer.start();
    }

    private void hideTypingIndicator() {
        if (typingTimer != null && typingTimer.isRunning()) typingTimer.stop();
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

    private void appendMessage(String chatId, String msg, boolean isUser) {
        JPanel currentPanel = chatPanels.get(chatId);
        if(currentPanel == null) return;

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 12, 0));

        JTextArea textArea = new JTextArea(msg);
        textArea.setFont(FONT_BODY);
        textArea.setForeground(isUser ? Color.WHITE : TEXT_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(12, 18, 12, 18));

        int textWidth = Math.min(msg.length() * 9 + 40, 520);
        textArea.setSize(new Dimension(textWidth, Short.MAX_VALUE));

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isUser) {
                    g2.setColor(USER_BUBBLE_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);
                } else {
                    g2.setColor(AI_BUBBLE_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_MD, RADIUS_MD);
                }
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.add(textArea, BorderLayout.CENTER);

        JPanel bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.X_AXIS));
        bubbleContainer.setOpaque(false);
        bubbleContainer.add(bubble);

        JPanel alignPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubbleContainer);

        wrapper.add(alignPanel, BorderLayout.CENTER);
        currentPanel.add(wrapper);

        if (isUser && currentPanel.getComponentCount() == 1) {
            String title = msg.length() > 25 ? msg.substring(0, 25) + "..." : msg;
            ChatHistoryItem item = historyItems.get(chatId);
            if (item != null) {
                item.setTitle(title); // Bỏ icon 💬
            }
        }

        currentPanel.revalidate();
        currentPanel.repaint();
        scrollToBottom();
    }

    private void loadChatHistoryFromServer() {
        lblStatus.setText("Loading history..."); // Bỏ icon ●
        lblStatus.setForeground(SUB_TEXT_COLOR);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                ServerConnection conn = ServerConnection.getInstance();
                if (!conn.isConnected()) conn.connect();
                return conn.sendCommand("HISTORY|" + username);
            }
            @Override
            protected void done() {
                lblStatus.setText("Online"); // Bỏ icon ●
                lblStatus.setForeground(ONLINE_COLOR);
                try {
                    String result = get();
                    if (result.startsWith("HISTORY_RESULT|") && result.length() > 15) {
                        String data = result.substring(15);
                        String[] records = data.split("###");
                        int maxCounter = 1;
                        for (String record : records) {
                            if (record.trim().isEmpty()) continue;
                            String[] parts = record.split("@@@");
                            if (parts.length >= 4) {
                                String chatId = parts[0];
                                String title = parts[1];
                                String sender = parts[2];
                                String msg = parts[3].replace("[NEWLINE]", "\n");

                                try {
                                    if (chatId.startsWith("ChatSession_")) {
                                        int num = Integer.parseInt(chatId.substring(12));
                                        if (num >= maxCounter) maxCounter = num + 1;
                                    }
                                } catch(Exception e) {}

                                if (!chatPanels.containsKey(chatId)) {
                                    JPanel newChatPanel = new JPanel();
                                    newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
                                    newChatPanel.setBackground(BG_COLOR);
                                    newChatPanel.setBorder(new EmptyBorder(32, 84, 32, 84));
                                    chatPanels.put(chatId, newChatPanel);
                                    addHistoryItem(chatId, title);
                                }
                                appendMessage(chatId, msg, sender.equals("USER"));
                            }
                        }
                        chatCounter = maxCounter;
                        if (!chatPanels.isEmpty()) {
                            String firstKey = chatPanels.keySet().iterator().next();
                            selectHistoryItem(firstKey);
                        } else createNewChat();
                    } else createNewChat();
                } catch (Exception ex) {
                    createNewChat();
                }
            }
        };
        worker.execute();
    }

    // Nút bo góc dùng chung cho toàn bộ giao diện
    private class RoundedButton extends JButton {
        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Thanh cuộn mảnh, bo tròn, hiện đại
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = BORDER_COLOR;
            trackColor = SURFACE_COLOR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
            // Track trong suốt để hoà vào nền
        }

        @Override
        protected void paintThumb(Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y, thumbBounds.width - 2, thumbBounds.height, 8, 8);
            g2.dispose();
        }
    }

    // Custom component for history items
    private class ChatHistoryItem extends JPanel {
        private JLabel lblTitle;
        private boolean selected = false;
        private String chatId;

        public ChatHistoryItem(String chatId, String title) {
            this.chatId = chatId;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTitle.setForeground(SUB_TEXT_COLOR);
            add(lblTitle, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackgroundColor(HOVER_COLOR);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackgroundColor(null);
                    }
                }
            });
        }

        private Color bgColor = null;

        private void setBackgroundColor(Color c) {
            this.bgColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (bgColor != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_SM, RADIUS_SM);
                g2.dispose();
            }
            super.paintComponent(g);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackgroundColor(HOVER_COLOR);
                lblTitle.setForeground(PRIMARY_COLOR);
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else {
                setBackgroundColor(null);
                lblTitle.setForeground(SUB_TEXT_COLOR);
                lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        }

        public void setTitle(String title) {
            lblTitle.setText(title);
        }
    }
}
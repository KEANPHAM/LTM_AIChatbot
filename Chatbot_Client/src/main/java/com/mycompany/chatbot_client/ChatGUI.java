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
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
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

        // --- NÚT TOGGLE MŨI TÊN & NÚT ĐỔI THEME SÁNG/TỐI ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeManager.getBgColor());

        // Bên TRÁI: Chứa nút mở/đóng Sidebar
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTop.setOpaque(false);
        JButton btnToggle = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ThemeManager.getTextColor() : ThemeManager.getSubTextColor());
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if (sidebar.isVisible()) {
                    g2.drawLine(cx + 4, cy - 6, cx - 4, cy);
                    g2.drawLine(cx - 4, cy, cx + 4, cy + 6);
                } else {
                    g2.drawLine(cx - 4, cy - 6, cx + 4, cy);
                    g2.drawLine(cx + 4, cy, cx - 4, cy + 6);
                }
                g2.dispose();
            }
        };
        btnToggle.setPreferredSize(new Dimension(40, 40));
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            btnToggle.repaint();
        });
        leftTop.add(btnToggle);
        topBar.add(leftTop, BorderLayout.WEST);

        // Bên PHẢI: Chứa nút Light/Dark Mode
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.setOpaque(false);
        
        JButton btnTheme = new JButton(ThemeManager.isDarkMode ? "☀" : "🌙");
        btnTheme.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // Dùng font Emoji để vẽ icon
        btnTheme.setForeground(ThemeManager.getTextColor());
        btnTheme.setContentAreaFilled(false);
        btnTheme.setBorderPainted(false);
        btnTheme.setFocusPainted(false);
        btnTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnTheme.addActionListener(e -> {
            // Đảo ngược trạng thái
            ThemeManager.isDarkMode = !ThemeManager.isDarkMode;
            btnTheme.setText(ThemeManager.isDarkMode ? "☀" : "🌙");
            
            // Ép toàn bộ cửa sổ vẽ lại với màu mới
            SwingUtilities.updateComponentTreeUI(this);
            getContentPane().setBackground(ThemeManager.getBgColor());
            topBar.setBackground(ThemeManager.getBgColor());
            repaint();
        });
        
        rightTop.add(btnTheme);
        topBar.add(rightTop, BorderLayout.EAST);

        mainArea.add(topBar, BorderLayout.NORTH);

        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        scrollPane.getViewport().setBackground(mainBgColor);
        mainArea.add(scrollPane, BorderLayout.CENTER);

        // --- BẮT ĐẦU: KHUNG NHẬP LIỆU LƠ LỬNG (FLOATING) CHUẨN GEMINI ---
        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.Y_AXIS));
        bottomWrapper.setBackground(ThemeManager.getBgColor());
        bottomWrapper.setBorder(new EmptyBorder(10, 50, 25, 50)); 

        JPanel floatingInputBar = new JPanel(new BorderLayout(10, 0));
        floatingInputBar.setBackground(ThemeManager.getSurfaceColor());
        // Tạo đường viền mảnh bo góc cho thanh nổi
        floatingInputBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            new EmptyBorder(5, 15, 5, 5) 
        ));

        txtInput = new JTextField();
        txtInput.setFont(ThemeManager.FONT_REGULAR);
        txtInput.setBackground(ThemeManager.getSurfaceColor());
        txtInput.setForeground(ThemeManager.getTextColor());
        txtInput.setCaretColor(ThemeManager.getTextColor());
        txtInput.setBorder(null); 

        btnSend = new JButton("➤");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnSend.setBackground(ThemeManager.getAccentColor());
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(45, 45));
        
        // Thêm hiệu ứng hover mượt mà cho nút gửi
        UIAnimator.addSmoothHover(btnSend, ThemeManager.getAccentColor(), new Color(20, 90, 200));

        floatingInputBar.add(txtInput, BorderLayout.CENTER);
        floatingInputBar.add(btnSend, BorderLayout.EAST);

        lblStatus = new JLabel(" Sẵn sàng.");
        lblStatus.setForeground(ThemeManager.getSubTextColor());
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setBorder(new EmptyBorder(8, 5, 0, 0));
        
        // Căn lề trái cho trạng thái
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.setBackground(ThemeManager.getBgColor());
        statusPanel.add(lblStatus);

        bottomWrapper.add(floatingInputBar);
        bottomWrapper.add(statusPanel);
        
        mainArea.add(bottomWrapper, BorderLayout.SOUTH);
        // --- KẾT THÚC: KHUNG NHẬP LIỆU ---
        add(mainArea, BorderLayout.CENTER);

        btnSend.addActionListener(this::handleSendMessage);
        txtInput.addActionListener(this::handleSendMessage);
        setLocationRelativeTo(null);

        loadChatHistoryFromServer();
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
                // SỬA: đóng luôn Socket dùng chung khi đăng xuất, để lần Login
                // sau (nếu đổi user khác) sẽ mở 1 kết nối/session mới sạch sẽ.
                ServerConnection.getInstance().disconnect();
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
private void addHistoryButtonWithTitle(String chatId, String title) {
    JLabel btn = new JLabel("💬  " + title); 
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
   private void addHistoryButton(String chatId) {
    addHistoryButtonWithTitle(chatId, "New Chat");
}
// THÊM HÀM NÀY: Cho phép đẩy tin nhắn vào bất kỳ Panel chỉ định nào (dùng khi khôi phục lịch sử)
private void appendMessageToPanel(String chatId, String msg, boolean isUser, boolean isSystem) {
    JPanel targetPanel = chatPanels.get(chatId); 
    if (targetPanel == null) return;

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
    targetPanel.add(wrapper);
    targetPanel.revalidate();
    targetPanel.repaint();

    scrollToBottom();
}

// THÊM HÀM NÀY: Gọi lên Server kéo lịch sử thô về và dựng lại giao diện nguyên bản
private void loadChatHistoryFromServer() {
    lblStatus.setText(" Đang đồng bộ hóa các phiên chat cũ...");
    lblStatus.setForeground(accentColor);

    SwingWorker<String, Void> worker = new SwingWorker<>() {
        @Override
        protected String doInBackground() throws Exception {
            ServerConnection conn = ServerConnection.getInstance();
            if (!conn.isConnected()) conn.connect();
            return conn.sendCommand("HISTORY|" + username);
        }

        @Override
        protected void done() {
            lblStatus.setText(" Sẵn sàng.");
            lblStatus.setForeground(textSecondary);
            try {
                String result = get();
                if (result.startsWith("HISTORY_RESULT|") && result.length() > 15) {
                    String data = result.substring("HISTORY_RESULT|".length());
                    if (data.trim().isEmpty()) {
                        createNewChat();
                        return;
                    }
                    
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
                            
                            // Dò tìm ID lớn nhất để không bị trùng lặp bộ đếm phiên chat mới
                            try {
                                if (chatId.startsWith("ChatSession_")) {
                                    int num = Integer.parseInt(chatId.substring(12));
                                    if (num >= maxCounter) maxCounter = num + 1;
                                }
                            } catch(Exception e) {}

                            // Tạo panel lưu trữ nếu phiên này chưa được khởi tạo trên giao diện
                            if (!chatPanels.containsKey(chatId)) {
                                JPanel newChatPanel = new JPanel();
                                newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
                                newChatPanel.setBackground(mainBgColor);
                                newChatPanel.setBorder(new EmptyBorder(10, 50, 10, 50));
                                
                                String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                                JLabel lblHeader = new JLabel("Hi " + displayName + ", what should we dive into today?");
                                lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
                                lblHeader.setForeground(textPrimary);
                                JPanel headerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
                                headerWrapper.setOpaque(false);
                                headerWrapper.setBorder(new EmptyBorder(50, 0, 50, 0));
                                headerWrapper.add(lblHeader);
                                newChatPanel.add(headerWrapper);

                                chatPanels.put(chatId, newChatPanel);
                                addHistoryButtonWithTitle(chatId, title);
                            }
                            
                            // Đẩy tin nhắn vào đúng vị trí phiên chat cũ
                            appendMessageToPanel(chatId, msg, sender.equals("USER"), false);
                        }
                    }
                    chatCounter = maxCounter;
                    
                    // Hiển thị phiên chat cuối cùng lên màn hình trung tâm
                    if (!chatPanels.isEmpty()) {
                        String lastChatId = null;
                        for (String k : chatPanels.keySet()) { lastChatId = k; }
                        currentChatId = lastChatId;
                        
                        JPanel outerChatPanel = new JPanel(new BorderLayout());
                        outerChatPanel.setBackground(mainBgColor);
                        outerChatPanel.add(chatPanels.get(currentChatId), BorderLayout.NORTH);
                        scrollPane.setViewportView(outerChatPanel);
                    } else {
                        createNewChat();
                    }
                } else {
                    createNewChat();
                }
            } catch (Exception ex) {
                createNewChat();
            }
        }
    };
    worker.execute();
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
// Nhận diện các lệnh đặc biệt (WEATHER|, PORT|, IP|) và giữ nguyên cú pháp gốc
// để gửi thẳng cho server. Nếu không khớp lệnh nào -> mặc định là CHAT với AI.
private String buildProtocolMessage(String rawInput) {
    String trimmed = rawInput.trim();
    int pipeIndex = trimmed.indexOf('|');

    if (pipeIndex > 0) {
        String keyword = trimmed.substring(0, pipeIndex).trim().toUpperCase();
        String rest = trimmed.substring(pipeIndex); // giữ nguyên dấu | + phần sau

        switch (keyword) {
            case "WEATHER":
            case "PORT":
            case "IP":
                return keyword + rest;
        }
    }

    return "CHAT|" + username + "|" + currentChatId + "|" + trimmed;
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
                try {
                    ServerConnection conn = ServerConnection.getInstance();

                    if (!conn.isConnected()) {
                        return "ERROR: Mất kết nối tới Server. Vui lòng đăng nhập lại!";
                    }

                    String rawMessage = buildProtocolMessage(command);
                    return conn.sendCommand(rawMessage);

                } catch (java.net.SocketTimeoutException te) {
                    return "ERROR: Quá 5 phút không nhận được phản hồi (Timeout).";
                } catch (Exception ex) {
                    return "ERROR: Lỗi mạng hoặc giải mã: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
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
        text.setFont(ThemeManager.FONT_REGULAR); 
        
        // Sửa màu chữ: Lỗi = Đỏ | User = Trắng (vì nền xanh) | AI = Màu chữ theo Theme
        if (isSystem) {
            text.setForeground(new Color(255, 100, 100));
        } else if (isUser) {
            text.setForeground(Color.WHITE);
        } else {
            text.setForeground(ThemeManager.getTextColor());
        }
        
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // CẢ AI VÀ USER ĐỀU CÓ BONG BÓNG MÀU ĐỘNG THEO THEME
                if (isUser) { 
                    g2.setColor(ThemeManager.getAccentColor()); // Nền xanh Gemini
                } else {
                    g2.setColor(ThemeManager.getSurfaceColor()); // Nền nổi Sáng/Tối
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); 
                g2.dispose();
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
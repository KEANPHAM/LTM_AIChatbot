package com.mycompany.chatbot_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.util.logging.*;

public class ChatGUI extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(ChatGUI.class.getName());
    private String serverIP = "localhost";

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(248, 249, 251);
    private static final Color BORDER_C    = new Color(218, 221, 228);
    private static final Color ACCENT      = new Color(99,  102, 241);
    private static final Color ACCENT_HOV  = new Color(79,  82,  221);
    private static final Color TEXT        = new Color(30,  32,  40);
    
    // Đã đổi màu bong bóng của Người dùng thành Xám nhạt
    private static final Color BUBBLE_USER = new Color(240, 244, 249); 

    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD,  14);

    // chatPane thay thế txtAreaChat để render bubble căn trái/phải
    private JTextPane chatPane;

    public ChatGUI() {
        initComponents();   // NetBeans — KHÔNG SỬA

        this.setTitle("AI Chatbot");
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(BG);

        // ── Thay JTextArea bằng JTextPane (gắn vào scroll cũ) ────────────────
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG);
        chatPane.setFont(FONT_BODY);
        chatPane.setMargin(new Insets(10, 10, 10, 10));
        jScrollPane1.setViewportView(chatPane);
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.getViewport().setBackground(BG);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(12);

        // ── Input ─────────────────────────────────────────────────────────────
        txtInput.setBackground(Color.WHITE);
        txtInput.setForeground(TEXT);
        txtInput.setCaretColor(ACCENT);
        txtInput.setFont(FONT_BODY);
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(BORDER_C, 22, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));

        // ── Nút Gửi ───────────────────────────────────────────────────────────
        jButton1.setText("Gửi");
        jButton1.setFont(FONT_BOLD);
        jButton1.setForeground(Color.WHITE);
        jButton1.setFocusPainted(false);
        jButton1.setBorderPainted(false);
        jButton1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // SỬA LỖI "..." TẠI ĐÂY: Giảm padding 2 bên từ 22 xuống 6 pixel để đủ chỗ hiển thị chữ
        jButton1.setBorder(new EmptyBorder(8, 6, 8, 6));
        jButton1.setUI(new RoundButtonUI(ACCENT, ACCENT_HOV, 22));

        askForServerIP();
    }

    // ── Thêm bubble vào chatPane ──────────────────────────────────────────────
    private void appendBubble(String text, boolean isUser) {
        StyledDocument doc = chatPane.getStyledDocument();

        // Style căn chỉnh
        Style base = chatPane.addStyle("base", null);
        StyleConstants.setFontFamily(base, "Segoe UI");
        StyleConstants.setFontSize(base, 14);

        // Paragraph align
        Style para = chatPane.addStyle("para", base);
        StyleConstants.setAlignment(para, isUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
        StyleConstants.setSpaceAbove(para, 6);
        StyleConstants.setSpaceBelow(para, 6);
        StyleConstants.setLeftIndent(para,  isUser ? 80 : 0);
        StyleConstants.setRightIndent(para, isUser ? 0 : 80);

        // SỬA MÀU CHỮ & MÀU NỀN TẠI ĐÂY
        StyleConstants.setForeground(para, TEXT); // Ép chữ thành màu đen/tối để dễ đọc trên nền xám
        
        // Nền người dùng = Xám nhạt, Nền AI = Trùng với màu màn hình (tạo cảm giác không viền)
        StyleConstants.setBackground(para, isUser ? BUBBLE_USER : BG);

        try {
            int start = doc.getLength();
            
            // CHỈ HIỆN CHỮ: Bỏ hoàn toàn chữ "✨ AI:"
            doc.insertString(start, " " + text + " \n", para);
            
            doc.setParagraphAttributes(start, doc.getLength() - start, para, false);
            // dòng trống ngăn cách
            Style blank = chatPane.addStyle("blank", null);
            StyleConstants.setSpaceBelow(blank, 4);
            doc.insertString(doc.getLength(), "\n", blank);
        } catch (BadLocationException ex) {
            logger.log(Level.WARNING, null, ex);
        }

        chatPane.setCaretPosition(doc.getLength());
    }

    private void askForServerIP() {
        String input = JOptionPane.showInputDialog(
            this,
            "Nhập địa chỉ IP của Server\n(Ví dụ: 192.168.1.10):",
            "localhost"
        );
        serverIP = (input == null || input.trim().isEmpty()) ? "localhost" : input.trim();
        if (input == null || input.trim().isEmpty())
            JOptionPane.showMessageDialog(this, "Dùng mặc định: localhost");
    }

    // ── Generated by NetBeans — KHÔNG SỬA ────────────────────────────────────
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaChat  = new javax.swing.JTextArea();
        txtInput     = new javax.swing.JTextField();
        jButton1     = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtAreaChat.setEditable(false);
        txtAreaChat.setColumns(20);
        txtAreaChat.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtAreaChat.setLineWrap(true);
        txtAreaChat.setRows(5);
        jScrollPane1.setViewportView(txtAreaChat);

        txtInput.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtInput.addActionListener(this::txtInputActionPerformed);

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jButton1.setText("Gửi");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(txtInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(38, 38, 38))
        );
        pack();
    }// </editor-fold>

    // ── Event handlers ────────────────────────────────────────────────────────
    private void txtInputActionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String userInput = txtInput.getText().trim();
        if (userInput.isEmpty()) return;

        String upper = userInput.toUpperCase();
        String messageToSend = (upper.startsWith("WEATHER|") || upper.startsWith("PORT|")
                             || upper.startsWith("IP|")      || upper.startsWith("CHAT|"))
            ? userInput : "CHAT|" + userInput;

        appendBubble(userInput, true);   // Tôi → phải
        txtInput.setText("");

        new Thread(() -> {
            try (Socket socket = new Socket(serverIP, 8888)) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream  in  = new DataInputStream(socket.getInputStream());

                out.writeUTF(AESUtil.encrypt(messageToSend));
                String response = AESUtil.decrypt(in.readUTF());

                SwingUtilities.invokeLater(() -> appendBubble(response, false));  // AI → trái
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> appendBubble("Lỗi kết nối! Kiểm tra IP hoặc Server.", false));
            }
        }).start();
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new ChatGUI().setVisible(true));
    }

    // ── Variables declaration — do not modify ─────────────────────────────────
    private javax.swing.JButton     jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea   txtAreaChat;
    private javax.swing.JTextField  txtInput;

    // ── RoundBorder ───────────────────────────────────────────────────────────
    static class RoundBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius, thickness;
        RoundBorder(Color c, int r, int t) { color = c; radius = r; thickness = t; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x+.5f, y+.5f, w-1, h-1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4,4,4,4); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    // ── RoundButtonUI ─────────────────────────────────────────────────────────
    static class RoundButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color normal, hover;
        private final int radius;
        RoundButtonUI(Color n, Color h, int r) { normal = n; hover = h; radius = r; }
        @Override public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(b.getModel().isRollover() ? hover : normal);
            g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), radius, radius));
            g2.dispose();
            super.paint(g, c);
        }
        @Override protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setContentAreaFilled(false);
        }
    }
}
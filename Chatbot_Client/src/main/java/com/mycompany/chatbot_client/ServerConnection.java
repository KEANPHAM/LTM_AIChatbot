package com.mycompany.chatbot_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * ServerConnection - Quản lý DUY NHẤT 1 kết nối Socket tới Server,
 * dùng chung xuyên suốt cả session (từ lúc Login thành công cho tới khi
 * đăng xuất / đóng app).
 *
 * LÝ DO CẦN CLASS NÀY:
 * Server (Chatbot_Server) lưu trạng thái "đã đăng nhập hay chưa" bằng 1
 * biến local (loggedInUser) trong Thread xử lý của từng Socket. Biến đó
 * CHỈ tồn tại trong đúng Thread/Socket đó. Nếu Client mở Socket MỚI cho
 * mỗi lệnh (như cách cũ), Server sẽ luôn thấy "chưa đăng nhập" vì mỗi
 * Socket là 1 Thread hoàn toàn mới, không nhớ được lần Login trước.
 *
 * => Client phải giữ ĐÚNG 1 Socket từ lúc Login, dùng lại cho mọi lệnh
 * sau đó (CHAT|, WEATHER|, PORT|, IP|...).
 *
 * Singleton đơn giản: 1 instance duy nhất cho toàn bộ chương trình Client.
 */
public class ServerConnection {

    private static ServerConnection instance;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int TIMEOUT_MS = 300000;

    private ServerConnection() {
        // private constructor - chỉ tạo qua getInstance()
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    /** Kiểm tra socket hiện tại còn sống và còn kết nối hay không. */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /** Mở kết nối mới tới Server. Gọi 1 lần lúc Login. */
    public synchronized void connect() throws Exception {
        if (isConnected()) {
            return; // đã có kết nối sống rồi, không mở thêm
        }
        socket = new Socket(HOST, PORT);
        socket.setSoTimeout(TIMEOUT_MS);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    /**
     * Gửi 1 lệnh (đã ở dạng plaintext, ví dụ "CHAT|user|noi dung") lên Server,
     * tự động mã hóa AES trước khi gửi, và giải mã phản hồi nhận về.
     * Dùng chung cho LOGIN|, REGISTER|, CHAT|, WEATHER|, PORT|, IP|...
     */
    public synchronized String sendCommand(String rawMessage) throws Exception {
        if (!isConnected()) {
            throw new java.net.SocketException("Chua ket noi toi Server. Goi connect() truoc.");
        }
        String encryptedMessage = AESUtil.encrypt(rawMessage);
        out.writeUTF(encryptedMessage);

        String encryptedResponse = in.readUTF();
        return AESUtil.decrypt(encryptedResponse);
    }

    /** Đóng kết nối, gọi khi đăng xuất hoặc tắt chương trình. */
    public synchronized void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            // bỏ qua lỗi khi đóng
        } finally {
            socket = null;
            out = null;
            in = null;
        }
    }
}
package com.mycompany.chatbot_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Quản lý duy nhất một kết nối Socket tới Chatbot Server trong suốt phiên.
 */
public class ServerConnection {

    private static ServerConnection instance;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private String host = "localhost";
    private int port = 8888;

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 300_000;

    private ServerConnection() {
        // Chỉ tạo thông qua getInstance().
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    /**
     * Thiết lập cả host và port trước khi gọi connect().
     * Ví dụ LAN: localhost:8888.
     * Ví dụ ngrok: 0.tcp.ap.ngrok.io:26674.
     */
    public synchronized void setEndpoint(String customHost, int customPort) {
        if (customHost == null || customHost.trim().isEmpty()) {
            customHost = "localhost";
        }
        if (customPort < 1 || customPort > 65535) {
            throw new IllegalArgumentException("Port phải nằm trong khoảng 1-65535.");
        }

        String normalizedHost = customHost.trim();
        if (normalizedHost.startsWith("tcp://")) {
            normalizedHost = normalizedHost.substring("tcp://".length()).trim();
        }

        // Nếu endpoint thay đổi trong lúc đang kết nối, đóng kết nối cũ.
        if (isConnected()
                && (!this.host.equalsIgnoreCase(normalizedHost) || this.port != customPort)) {
            disconnect();
        }

        this.host = normalizedHost;
        this.port = customPort;
    }

    /** Giữ lại để tương thích với mã cũ chỉ gọi setHost(). */
    public synchronized void setHost(String customHost) {
        setEndpoint(customHost, this.port);
    }

    public synchronized void setPort(int customPort) {
        setEndpoint(this.host, customPort);
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized int getPort() {
        return port;
    }

    /** Kiểm tra socket hiện tại còn sống và còn kết nối hay không. */
    public synchronized boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    /** Mở kết nối mới tới Server. Gọi một lần lúc đăng nhập. */
    public synchronized void connect() throws Exception {
        if (isConnected()) {
            return;
        }

        disconnect();

        Socket newSocket = new Socket();
        try {
            newSocket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            newSocket.setSoTimeout(READ_TIMEOUT_MS);

            socket = newSocket;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            System.out.println("Đã kết nối tới Server: " + host + ":" + port);
        } catch (Exception ex) {
            try {
                newSocket.close();
            } catch (Exception ignored) {
                // Không cần xử lý thêm.
            }
            socket = null;
            out = null;
            in = null;
            throw ex;
        }
    }

    /**
     * Mã hóa lệnh bằng AES, gửi lên Server rồi giải mã phản hồi.
     */
    public synchronized String sendCommand(String rawMessage) throws Exception {
        if (!isConnected()) {
            throw new java.net.SocketException(
                    "Chưa kết nối tới Server. Hãy gọi connect() trước."
            );
        }

        String encryptedMessage = AESUtil.encrypt(rawMessage);
        out.writeUTF(encryptedMessage);
        out.flush();

        String encryptedResponse = in.readUTF();
        return AESUtil.decrypt(encryptedResponse);
    }

    /** Đóng kết nối khi đăng xuất hoặc tắt chương trình. */
    public synchronized void disconnect() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        } finally {
            socket = null;
            out = null;
            in = null;
        }
    }
}

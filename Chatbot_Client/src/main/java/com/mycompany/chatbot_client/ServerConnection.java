package com.mycompany.chatbot_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Quản lý duy nhất một kết nối Socket tới Chatbot Server.
 *
 * Hỗ trợ:
 * - localhost/LAN: localhost:8888
 * - ngrok TCP: 0.tcp.ap.ngrok.io:26674
 * - Chủ động kết nối lại khi Socket cũ bị treo hoặc tunnel ngrok thay đổi.
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
     * Thiết lập host và port trước khi kết nối.
     *
     * Ví dụ:
     * setEndpoint("localhost", 8888);
     * setEndpoint("0.tcp.ap.ngrok.io", 26674);
     */
    public synchronized void setEndpoint(String customHost, int customPort) {
        String normalizedHost = normalizeHost(customHost);

        if (customPort < 1 || customPort > 65535) {
            throw new IllegalArgumentException(
                    "Port phải nằm trong khoảng từ 1 đến 65535."
            );
        }

        boolean endpointChanged =
                !this.host.equalsIgnoreCase(normalizedHost)
                || this.port != customPort;

        if (endpointChanged) {
            disconnect();
        }

        this.host = normalizedHost;
        this.port = customPort;
    }

    /**
     * Giữ lại để tương thích với code cũ chỉ gọi setHost().
     */
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

    public synchronized String getEndpoint() {
        return host + ":" + port;
    }

    /**
     * Socket.isConnected() chỉ cho biết Socket đã từng kết nối.
     * Các điều kiện còn lại giúp loại bớt Socket đã đóng/shutdown.
     */
    public synchronized boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown()
                && in != null
                && out != null;
    }

    /**
     * Kết nối tới Server nếu chưa có kết nối hiện tại.
     */
    public synchronized void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        openNewConnection();
    }

    /**
     * Luôn đóng Socket cũ và tạo một kết nối mới.
     *
     * Dùng cho thao tác đăng ký để tránh trường hợp Socket cũ vẫn báo
     * isConnected() nhưng tunnel ngrok hoặc phía Server đã ngắt.
     */
    public synchronized void reconnect() throws IOException {
        disconnect();
        openNewConnection();
    }

    /**
     * Tạo kết nối TCP mới.
     */
    private void openNewConnection() throws IOException {
        disconnect();

        Socket newSocket = new Socket();

        try {
            System.out.println(
                    "[SOCKET] Dang ket noi toi " + host + ":" + port + "..."
            );

            newSocket.connect(
                    new InetSocketAddress(host, port),
                    CONNECT_TIMEOUT_MS
            );
            newSocket.setSoTimeout(READ_TIMEOUT_MS);
            newSocket.setTcpNoDelay(true);
            newSocket.setKeepAlive(true);

            socket = newSocket;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            System.out.println(
                    "[SOCKET] Da ket noi toi " + host + ":" + port
            );
        } catch (IOException ex) {
            try {
                newSocket.close();
            } catch (IOException ignored) {
            }

            socket = null;
            out = null;
            in = null;

            throw new IOException(
                    "Khong the ket noi toi " + host + ":" + port
                            + ". " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Mã hóa lệnh bằng AES, gửi lên Server rồi giải mã phản hồi.
     */
    public synchronized String sendCommand(String rawMessage) throws Exception {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Lệnh gửi lên Server không được rỗng.");
        }

        if (!isConnected()) {
            throw new SocketException(
                    "Chưa kết nối tới Server. Endpoint hiện tại: " + getEndpoint()
            );
        }

        try {
            System.out.println(
                    "[SOCKET] Gui lenh: " + commandName(rawMessage)
                            + " toi " + getEndpoint()
            );

            String encryptedMessage = AESUtil.encrypt(rawMessage);

            out.writeUTF(encryptedMessage);
            out.flush();

            String encryptedResponse = in.readUTF();
            String response = AESUtil.decrypt(encryptedResponse);

            System.out.println(
                    "[SOCKET] "+ "Da nhan phan hoi tu server."
            );

            return response;
        } catch (Exception ex) {
            disconnect();

            throw new IOException(
                    "Lỗi gửi/nhận dữ liệu với Server "
                            + getEndpoint() + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Đóng kết nối hiện tại.
     */
    public synchronized void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        socket = null;
        out = null;
        in = null;
    }

    private static String normalizeHost(String customHost) {
        if (customHost == null || customHost.trim().isEmpty()) {
            return "localhost";
        }

        String normalized = customHost.trim();

        if (normalized.regionMatches(true, 0, "tcp://", 0, 6)) {
            normalized = normalized.substring(6).trim();
        }

        // Nếu người dùng truyền nhầm host:port vào setHost(),
        // chỉ lấy phần host. Port vẫn phải đặt bằng setPort/setEndpoint.
        int colonIndex = normalized.lastIndexOf(':');
        if (colonIndex > 0 && normalized.indexOf(':') == colonIndex) {
            String possiblePort = normalized.substring(colonIndex + 1);

            try {
                Integer.parseInt(possiblePort);
                normalized = normalized.substring(0, colonIndex);
            } catch (NumberFormatException ignored) {
                // Không phải dạng host:port, giữ nguyên.
            }
        }

        return normalized;
    }

    /**
     * Chỉ log tên lệnh, không log mật khẩu hoặc nội dung nhạy cảm.
     */
    private static String commandName(String rawMessage) {
        int separatorIndex = rawMessage.indexOf('|');

        if (separatorIndex < 0) {
            return rawMessage;
        }

        return rawMessage.substring(0, separatorIndex);
    }
}

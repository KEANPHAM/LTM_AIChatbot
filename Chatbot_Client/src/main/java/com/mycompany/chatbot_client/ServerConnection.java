package com.mycompany.chatbot_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;

import javax.crypto.SecretKey;

/**
 * Quan ly mot ket noi Socket toi Chatbot Server.
 *
 * Khi tao ket noi moi:
 * 1. Client tao cap khoa RSA.
 * 2. Client gui RSA Public Key cho Server.
 * 3. Server sinh khoa AES rieng cho phien.
 * 4. Client nhan va giai ma khoa AES bang RSA Private Key.
 */
public class ServerConnection {

    private static ServerConnection instance;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private SecretKey sessionKey;

    private String host = "localhost";
    private int port = 8888;

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 300_000;

    private static final String KEY_EXCHANGE_PREFIX = "KEY_EXCHANGE|";
    private static final String SESSION_KEY_PREFIX = "SESSION_KEY|";

    private ServerConnection() {
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public synchronized void setEndpoint(
            String customHost,
            int customPort
    ) {
        String normalizedHost = normalizeHost(customHost);

        if (customPort < 1 || customPort > 65535) {
            throw new IllegalArgumentException(
                    "Port phai nam trong khoang tu 1 den 65535."
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

    public synchronized boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown()
                && in != null
                && out != null
                && sessionKey != null;
    }

    public synchronized void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        openNewConnection();
    }

    public synchronized void reconnect() throws IOException {
        disconnect();
        openNewConnection();
    }

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

            exchangeSessionKey();

            System.out.println(
                    "[SOCKET] Da ket noi va trao doi khoa AES voi "
                            + host + ":" + port
            );
        } catch (Exception ex) {
            closeQuietly(newSocket);

            socket = null;
            out = null;
            in = null;
            sessionKey = null;

            throw new IOException(
                    "Khong the ket noi toi " + host + ":" + port
                            + ". " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Bat tay trao doi khoa truoc khi gui cac lenh LOGIN, REGISTER, CHAT...
     */
    private void exchangeSessionKey() throws Exception {
        KeyPair clientKeyPair = RSAUtil.generateKeyPair();

        String publicKeyBase64 =
                RSAUtil.publicKeyToBase64(clientKeyPair);

        out.writeUTF(KEY_EXCHANGE_PREFIX + publicKeyBase64);
        out.flush();

        String response = in.readUTF();
        if (!response.startsWith(SESSION_KEY_PREFIX)) {
            throw new IOException(
                    "Server khong tra ve khoa AES hop le."
            );
        }

        String encryptedSessionKey = response.substring(
                SESSION_KEY_PREFIX.length()
        );

        sessionKey = RSAUtil.decryptAESKey(
                encryptedSessionKey,
                clientKeyPair.getPrivate()
        );
    }

    public synchronized String sendCommand(
            String rawMessage
    ) throws Exception {

        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Lenh gui len Server khong duoc rong."
            );
        }

        if (!isConnected()) {
            throw new SocketException(
                    "Chua ket noi toi Server. Endpoint hien tai: "
                            + getEndpoint()
            );
        }

        try {
            System.out.println(
                    "[SOCKET] Gui lenh: " + commandName(rawMessage)
                            + " toi " + getEndpoint()
            );

            String encryptedMessage = AESUtil.encrypt(
                    rawMessage,
                    sessionKey
            );

            out.writeUTF(encryptedMessage);
            out.flush();

            String encryptedResponse = in.readUTF();
            String response = AESUtil.decrypt(
                    encryptedResponse,
                    sessionKey
            );

            System.out.println(
                    "[SOCKET] Da nhan phan hoi tu server."
            );

            return response;
        } catch (Exception ex) {
            disconnect();

            throw new IOException(
                    "Loi gui/nhan du lieu voi Server "
                            + getEndpoint() + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    public synchronized void disconnect() {
        closeQuietly(socket);

        socket = null;
        out = null;
        in = null;
        sessionKey = null;
    }

    private static void closeQuietly(Socket targetSocket) {
        if (targetSocket != null) {
            try {
                targetSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String normalizeHost(String customHost) {
        if (customHost == null || customHost.trim().isEmpty()) {
            return "localhost";
        }

        String normalized = customHost.trim();

        if (normalized.regionMatches(true, 0, "tcp://", 0, 6)) {
            normalized = normalized.substring(6).trim();
        }

        int colonIndex = normalized.lastIndexOf(':');
        if (colonIndex > 0
                && normalized.indexOf(':') == colonIndex) {

            String possiblePort = normalized.substring(colonIndex + 1);

            try {
                Integer.parseInt(possiblePort);
                normalized = normalized.substring(0, colonIndex);
            } catch (NumberFormatException ignored) {
            }
        }

        return normalized;
    }

    private static String commandName(String rawMessage) {
        int separatorIndex = rawMessage.indexOf('|');

        if (separatorIndex < 0) {
            return rawMessage;
        }

        return rawMessage.substring(0, separatorIndex);
    }
}

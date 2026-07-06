package com.mycompany.chatbot_server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    
    // Đường dẫn kết nối đến file SQLite cục bộ
    private static final String DB_URL = "jdbc:sqlite:ChatbotDB.db";

    public DatabaseHelper() {
        initializeDatabase();
    }

    /**
     * Khởi tạo DB và tạo bảng Users nếu chưa tồn tại.
     */
    private void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Users ("
                + "username TEXT PRIMARY KEY, "
                + "password_hash TEXT NOT NULL, "
                + "current_summary TEXT DEFAULT ''"
                + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo Database: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem tài khoản đã tồn tại chưa.
     */
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Nếu có kết quả trả về true
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra userExists: " + e.getMessage());
        }
        return false;
    }

    /**
     * Băm mật khẩu và lưu user mới.
     * @return true nếu đăng ký thành công, false nếu username đã tồn tại.
     */
    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }

        String sql = "INSERT INTO Users(username, password_hash, current_summary) VALUES(?, ?, '')";
        String hashedPassword = SecurityUtil.hashPassword(password);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Lỗi đăng ký user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra đăng nhập bằng cách đối chiếu mật khẩu băm.
     */
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash FROM Users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String inputHash = SecurityUtil.hashPassword(password);
                    return storedHash.equals(inputHash);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi authenticateUser: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật đoạn tóm tắt hội thoại. Giới hạn tối đa 500 ký tự.
     */
    public void updateSummary(String username, String newSummary) {
        if (newSummary == null) {
            newSummary = "";
        }
        
        // Cắt chuỗi và loại bỏ khoảng trắng thừa để chống tràn Database
        newSummary = newSummary.trim();
        if (newSummary.length() > 500) {
            newSummary = newSummary.substring(0, 500);
        }

        String sql = "UPDATE Users SET current_summary = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newSummary);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Lỗi updateSummary: " + e.getMessage());
        }
    }

    /**
     * Lấy đoạn tóm tắt hiện tại để Server ghép vào API AI.
     */
    public String getSummary(String username) {
        String sql = "SELECT current_summary FROM Users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("current_summary");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi getSummary: " + e.getMessage());
        }
        return "";
    }

    /**
     * Lấy toàn bộ thông tin User, trả về Object User.
     */
    public User getUserInfo(String username) {
        String sql = "SELECT username, password_hash, current_summary FROM Users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("current_summary")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi getUserInfo: " + e.getMessage());
        }
        return null;
    }
}
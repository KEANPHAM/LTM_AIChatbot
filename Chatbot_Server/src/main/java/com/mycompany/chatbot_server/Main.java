package com.mycompany.chatbot_server;
public class Main {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST HỆ THỐNG DATABASE & SECURITY ===");

        // 1. Khởi tạo Database
        DatabaseHelper dbHelper = new DatabaseHelper();
        String testUsername = "nhat_test";
        String testPassword = "SuperSecretPassword123";

        // 2. Kiểm tra Đăng ký
        System.out.println("\n[1] TEST ĐĂNG KÝ (REGISTER)");
        boolean isRegistered = dbHelper.registerUser(testUsername, testPassword);
        System.out.println("- Đăng ký tài khoản mới: " + (isRegistered ? "THÀNH CÔNG (Pass)" : "THẤT BẠI (Fail) - Có thể User đã tồn tại từ trước"));

        boolean isRegisteredAgain = dbHelper.registerUser(testUsername, "mat_khau_khac");
        System.out.println("- Đăng ký lại trùng username: " + (!isRegisteredAgain ? "ĐÃ CHẶN ĐÚNG (Pass)" : "LỖI CHẶN (Fail)"));

        // 3. Kiểm tra Đăng nhập
        System.out.println("\n[2] TEST ĐĂNG NHẬP (AUTHENTICATE)");
        boolean isLoginSuccess = dbHelper.authenticateUser(testUsername, testPassword);
        System.out.println("- Đăng nhập đúng mật khẩu: " + (isLoginSuccess ? "THÀNH CÔNG (Pass)" : "THẤT BẠI (Fail)"));

        boolean isLoginFail = dbHelper.authenticateUser(testUsername, "SaiMatKhau");
        System.out.println("- Đăng nhập sai mật khẩu: " + (!isLoginFail ? "TỪ CHỐI ĐÚNG (Pass)" : "LỖI BẢO MẬT (Fail)"));

        // 4. Kiểm tra Cập nhật & Lấy Summary
        System.out.println("\n[3] TEST LƯU & TRÍCH XUẤT SUMMARY");
        String fakeSummary = "User: Xin chào. Bot: Chào bạn, tôi có thể giúp gì?";
        dbHelper.updateSummary(testUsername, fakeSummary);
        
        String retrievedSummary = dbHelper.getSummary(testUsername);
        System.out.println("- Summary lấy từ DB: " + retrievedSummary);
        System.out.println("- Trùng khớp dữ liệu: " + (fakeSummary.equals(retrievedSummary) ? "ĐÚNG (Pass)" : "SAI (Fail)"));

        // 5. Kiểm tra lấy User Object và mã Hash
        System.out.println("\n[4] TEST TRÍCH XUẤT OBJECT & KIỂM TRA HASH");
        User userObj = dbHelper.getUserInfo(testUsername);
        if (userObj != null) {
            System.out.println("- Username: " + userObj.getUsername());
            System.out.println("- Password Hash (SHA-256): " + userObj.getPasswordHash());
            System.out.println("- Thuật toán băm hoạt động: " + (userObj.getPasswordHash().length() == 64 ? "CHUẨN SHA-256 (Pass)" : "LỖI ĐỘ DÀI (Fail)"));
        } else {
            System.out.println("- Lỗi: Không lấy được thông tin User.");
        }

        System.out.println("\n=== HOÀN TẤT TEST ===");
    }
}
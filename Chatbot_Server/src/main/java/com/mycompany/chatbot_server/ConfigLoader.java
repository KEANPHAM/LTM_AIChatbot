package com.mycompany.chatbot_server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * ConfigLoader - Đọc các giá trị nhạy cảm (AES key, API key...) từ file
 * config.properties nằm NGOÀI source code, thay vì hardcode trong .java.
 *
 * File config.properties KHÔNG được commit lên Git (đã thêm vào .gitignore).
 * Chỉ commit file config.properties.example làm mẫu cho người khác biết
 * cần điền gì.
 *
 * Thứ tự ưu tiên đọc giá trị:
 *   1. Biến môi trường (Environment Variable) - ưu tiên cao nhất.
 *   2. File config.properties - tự dò qua NHIỀU vị trí khả thi, vì các
 *      IDE/launcher khác nhau (VS Code, NetBeans, java -jar, mvn exec)
 *      có thể chạy chương trình với "working directory" khác nhau.
 */
public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static Properties properties;
    private static boolean loaded = false;
    private static String foundAt = null; // lưu lại đã đọc được từ đâu, để debug

    // Các vị trí khả thi sẽ được dò qua, theo thứ tự ưu tiên
    private static String[] candidatePaths() {
        return new String[] {
            CONFIG_FILE_NAME,                 // working directory hiện tại
            "./" + CONFIG_FILE_NAME,
            "../" + CONFIG_FILE_NAME,         // nếu đang chạy từ trong target/ hoặc src/
            "Chatbot_Server/" + CONFIG_FILE_NAME, // nếu chạy từ thư mục gốc workspace (vd D:\LTM\DoAn)
        };
    }

    // Nạp file config.properties (nếu có) đúng 1 lần, dùng lại cho các lần gọi sau
    private static synchronized void loadPropertiesFileIfNeeded() {
        if (loaded) {
            return;
        }
        properties = new Properties();

        for (String candidate : candidatePaths()) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                try (InputStream input = new FileInputStream(path.toFile())) {
                    properties.load(input);
                    foundAt = path.toString();
                    System.out.println("[ConfigLoader] Da nap file: " + foundAt);
                    loaded = true;
                    return;
                } catch (IOException e) {
                    System.out.println("[ConfigLoader] Tim thay file nhung khong doc duoc: " + path + " (" + e.getMessage() + ")");
                }
            }
        }

        System.out.println("[ConfigLoader] Khong tim thay " + CONFIG_FILE_NAME + " o cac vi tri da thu:");
        for (String candidate : candidatePaths()) {
            System.out.println("    - " + Paths.get(candidate).toAbsolutePath().normalize());
        }
        System.out.println("    Working directory hien tai (user.dir): " + Paths.get("").toAbsolutePath());
        System.out.println("[ConfigLoader] Se thu doc bien moi truong thay the.");
        loaded = true;
    }

    /**
     * Lấy giá trị theo key, ưu tiên biến môi trường trước, sau đó tới file config.
     * Nếu không có ở đâu cả -> throw lỗi rõ ràng, không trả về null hay chuỗi rỗng.
     */
    public static String get(String key) {
        // 1. Ưu tiên biến môi trường
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        // 2. Thử đọc từ file config.properties (tự dò nhiều vị trí)
        loadPropertiesFileIfNeeded();
        String fileValue = properties.getProperty(key);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }

        // 3. Không tìm thấy ở đâu cả -> dừng ngay, báo lỗi rõ ràng
        throw new IllegalStateException(
                "[ConfigLoader] KHONG TIM THAY gia tri cho '" + key + "'.\n"
                + "Hay lam 1 trong 2 cach:\n"
                + "  (1) Tao file " + CONFIG_FILE_NAME + " dat tai thu muc goc cua project "
                + "(ngang hang voi pom.xml), co dong: " + key + "=gia_tri_cua_ban\n"
                + "  (2) Hoac set bien moi truong " + key + " truoc khi chay chuong trinh.\n"
                + "Xem file config.properties.example de biet cac key can co.\n"
                + "Working directory hien tai (user.dir): " + Paths.get("").toAbsolutePath());
    }

    // Các hằng số tên key, dùng chung để tránh gõ sai chuỗi ở nhiều nơi
    public static final String AES_SECRET_KEY = "AES_SECRET_KEY";
    public static final String OPENWEATHER_API_KEY = "OPENWEATHER_API_KEY";
    public static final String GROQ_API_KEY = "GROQ_API_KEY";
}
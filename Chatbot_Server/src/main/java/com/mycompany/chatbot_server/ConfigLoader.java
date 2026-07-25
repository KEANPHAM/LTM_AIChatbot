package com.mycompany.chatbot_server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Doc API key tu bien moi truong hoac file config.properties.
 *
 * Khoa AES khong nam trong file nay nua. Server tu sinh mot khoa AES
 * rieng cho moi phien Client ket noi.
 */
public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static Properties properties;
    private static boolean loaded = false;
    private static String foundAt = null;

    private static String[] candidatePaths() {
        return new String[] {
            CONFIG_FILE_NAME,
            "./" + CONFIG_FILE_NAME,
            "../" + CONFIG_FILE_NAME,
            "Chatbot_Server/" + CONFIG_FILE_NAME
        };
    }

    private static synchronized void loadPropertiesFileIfNeeded() {
        if (loaded) {
            return;
        }

        properties = new Properties();

        for (String candidate : candidatePaths()) {
            Path path = Paths.get(candidate)
                    .toAbsolutePath()
                    .normalize();

            if (Files.exists(path)) {
                try (InputStream input =
                        new FileInputStream(path.toFile())) {

                    properties.load(input);
                    foundAt = path.toString();

                    System.out.println(
                            "[ConfigLoader] Da nap file: " + foundAt
                    );

                    loaded = true;
                    return;
                } catch (IOException e) {
                    System.out.println(
                            "[ConfigLoader] Khong doc duoc file: "
                                    + path + " (" + e.getMessage() + ")"
                    );
                }
            }
        }

        System.out.println(
                "[ConfigLoader] Khong tim thay "
                        + CONFIG_FILE_NAME
                        + ", se thu doc bien moi truong."
        );

        loaded = true;
    }

    public static String get(String key) {
        String envValue = System.getenv(key);

        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        loadPropertiesFileIfNeeded();

        String fileValue = properties.getProperty(key);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }

        throw new IllegalStateException(
                "[ConfigLoader] KHONG TIM THAY gia tri cho '"
                        + key + "'.\n"
                        + "Hay them vao config.properties hoac bien moi truong.\n"
                        + "Working directory: "
                        + Paths.get("").toAbsolutePath()
        );
    }

    public static final String OPENWEATHER_API_KEY =
            "OPENWEATHER_API_KEY";
    public static final String GROQ_API_KEY =
            "GROQ_API_KEY";
}

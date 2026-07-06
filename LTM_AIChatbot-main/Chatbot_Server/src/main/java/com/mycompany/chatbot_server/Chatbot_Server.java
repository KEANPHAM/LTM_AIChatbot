package com.mycompany.chatbot_server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chatbot_Server {
    private static DatabaseHelper dbHelper = new DatabaseHelper();
    // =========================================================
    // 1. CÁC HÀM PHỤ TRỢ
    // =========================================================
    public static String getWeather(String city) {
        try {
            // SỬA: không hardcode key nữa, lấy qua ConfigLoader
            // (đọc từ config.properties hoặc biến môi trường OPENWEATHER_API_KEY)
            String apiKey = ConfigLoader.get(ConfigLoader.OPENWEATHER_API_KEY);
            String cityEncoded = URLEncoder.encode(city, "UTF-8");
            String urlString = "http://api.openweathermap.org/data/2.5/forecast?q="
                    + cityEncoded + "&appid=" + apiKey + "&units=metric&lang=vi&cnt=8";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Khong the lay thong tin thoi tiet luc nay!";
        }
    }

    public static String removeAccents(String str) {
        try {
            String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
        } catch (Exception e) {
            return str;
        }
    }

    public static String scanPort(String ip, int startPort, int endPort) {
        StringBuilder openPorts = new StringBuilder("Các port đang mở trên " + ip + ": ");
        boolean found = false;

        if (endPort < startPort || (endPort - startPort > 1000)) {
            return "Lỗi: Dải port không hợp lệ hoặc quá lớn (Giới hạn tối đa 1000 port/lần quét).";
        }

        for (int port = startPort; port <= endPort; port++) {
            try {
                Socket socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(ip, port), 200);
                socket.close();

                openPorts.append(port).append(", ");
                found = true;
            } catch (Exception e) {
                // Port đang đóng, bỏ qua
            }
        }

        if (!found) {
            return "Không tìm thấy port nào đang mở trên IP " + ip + " trong khoảng từ " + startPort + " đến " + endPort + ".";
        }

        return openPorts.substring(0, openPorts.length() - 2);
    }

    // =========================================================
    // GỌI AI GROQ - DÙNG ORG.JSON
    // =========================================================
    public static String goiAI(String systemMessage, String userMessage) {
        try {
            // SỬA: không hardcode key thật trong source nữa.
            // Key cũ "gsk_...HNBB" đã bị lộ công khai trên GitHub -> PHẢI revoke
            // (hủy) key đó trên console.groq.com và tạo key mới, rồi điền key
            // mới vào config.properties / biến môi trường GROQ_API_KEY.
            String apiKey = ConfigLoader.get(ConfigLoader.GROQ_API_KEY);
            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();

            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemMessage);
            messages.put(sysMsg);

            JSONObject usrMsg = new JSONObject();
            usrMsg.put("role", "user");
            usrMsg.put("content", userMessage);
            messages.put(usrMsg);

            payload.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() != 200) {
                return "Groq API báo lỗi (Mã lỗi: " + conn.getResponseCode() + ").";
            }

            StringBuilder responseStr = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseStr.append(responseLine.trim());
                }
            }

            JSONObject jsonResponse = new JSONObject(responseStr.toString());
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối AI: " + e.getMessage();
        }
    }

    // =========================================================
    // HÀM MAIN: KHỞI ĐỘNG SERVER & XỬ LÝ ĐA LUỒNG (MULTI-THREADING)
    // =========================================================
    public static void main(String[] args) {
        try {
            int port = 8888;
            System.out.println("Dang mo Port " + port + ", cho Client ket noi...");
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client da ket noi thanh cong tu: " + socket.getRemoteSocketAddress());

                new Thread(() -> {
                    String loggedInUser = null;

                    try {
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        while (true) {
                            String messageTuClient = in.readUTF();
                            System.out.println("Server nhan duoc chuoi ma hoa: " + messageTuClient);

                            String thongDiepThat = AESUtil.decrypt(messageTuClient);
                            System.out.println("Noi dung Client gui: " + thongDiepThat);

                            String cauTraLoi = "";

                            // === LOGIN & REGISTER ===
                            if (thongDiepThat.toUpperCase().startsWith("LOGIN|")) {
                                String[] parts = thongDiepThat.split("\\|");
                                if (parts.length >= 3) {
                                    String user = parts[1];
                                    String pass = parts[2];

                                    boolean loginSuccess = true; // Giả lập DB

                                    if (loginSuccess) {
                                        loggedInUser = user;
                                        cauTraLoi = "LOGIN_RESULT|OK";
                                        System.out.println("User " + user + " dang nhap thanh cong!");
                                    } else {
                                        cauTraLoi = "LOGIN_RESULT|FAIL";
                                    }
                                } else {
                                    cauTraLoi = "LOGIN_RESULT|FAIL_FORMAT";
                                }
                            } else if (thongDiepThat.toUpperCase().startsWith("REGISTER|")) {
                                cauTraLoi = "REGISTER_RESULT|OK";
                            }

                            // === CHAT AI CÓ NGỮ CẢNH ===
                            else if (thongDiepThat.toUpperCase().startsWith("CHAT|")) {
                                if (loggedInUser == null) {
                                    cauTraLoi = "Lỗi: Vui lòng đăng nhập trước khi dùng AI!";
                                } else {
                                    String cauHoi = thongDiepThat.substring(5);

                                    String summaryCu = "Chưa có ngữ cảnh hội thoại.";

                                    String systemPrompt = "Ngữ cảnh trước đây: [" + summaryCu + "]. Dựa vào ngữ cảnh này, hãy trả lời câu hỏi của người dùng.";

                                    cauTraLoi = goiAI(systemPrompt, cauHoi);

                                    final String finalTraLoi = cauTraLoi;
                                    final String finalLoggedInUser = loggedInUser; // copy final cho lambda dùng (tránh lỗi effectively final)
                                    new Thread(() -> {
                                        try {
                                            String summaryPrompt = "Hãy tóm tắt ngắn gọn thành 1-2 câu chuỗi hội thoại sau. "
                                                    + "Ngữ cảnh cũ: [" + summaryCu + "]. "
                                                    + "Người dùng hỏi: [" + cauHoi + "]. "
                                                    + "AI đáp: [" + finalTraLoi + "].";

                                            String newSummary = goiAI("Bạn là chuyên gia tóm tắt dữ liệu.", summaryPrompt);

                                            System.out.println("Da tom tat va luu DB ngam cho user: " + finalLoggedInUser);

                                        } catch (Exception e) {
                                            System.err.println("Loi luong tom tat ngam cua " + finalLoggedInUser + ": " + e.getMessage());
                                        }
                                    }).start();
                                }
                            }

                            // === WEATHER ===
                            else if (thongDiepThat.toUpperCase().startsWith("WEATHER|")) {
                                String tenThanhPho = thongDiepThat.substring(8).trim();
                                String thanhPhoKhongDau = removeAccents(tenThanhPho);
                                String jsonThoiTiet = getWeather(thanhPhoKhongDau);

                                if (jsonThoiTiet.contains("Khong the lay thong tin")) {
                                    cauTraLoi = "Lỗi: Không thể kết nối đến máy chủ thời tiết!";
                                } else {
                                    try {
                                        JSONObject jsonObj = new JSONObject(jsonThoiTiet);
                                        JSONArray danhSachDuBao = jsonObj.getJSONArray("list");

                                        StringBuilder sb = new StringBuilder();
                                        sb.append("=== DỰ BÁO THỜI TIẾT: ").append(tenThanhPho.toUpperCase()).append(" (24H TỚI) ===\n");

                                        for (int i = 0; i < danhSachDuBao.length(); i++) {
                                            JSONObject thoiDiem = danhSachDuBao.getJSONObject(i);
                                            String thoiGianGoc = thoiDiem.getString("dt_txt");
                                            String ngayThang = thoiGianGoc.substring(8, 10) + "/" + thoiGianGoc.substring(5, 7);
                                            String gioPhut = thoiGianGoc.substring(11, 16);

                                            double nhietDo = thoiDiem.getJSONObject("main").getDouble("temp");
                                            String moTa = thoiDiem.getJSONArray("weather").getJSONObject(0).getString("description");
                                            moTa = moTa.substring(0, 1).toUpperCase() + moTa.substring(1);

                                            sb.append(String.format("[%s - %s] Nhiệt độ: %.1f°C | %s\n", gioPhut, ngayThang, nhietDo, moTa));
                                        }
                                        sb.append("------------------------------------------");
                                        cauTraLoi = sb.toString();
                                    } catch (Exception e) {
                                        cauTraLoi = "Lỗi khi bóc tách dữ liệu thời tiết: " + e.getMessage();
                                    }
                                }
                            }

                            // === PORT ===
                            else if (thongDiepThat.toUpperCase().startsWith("PORT|")) {
                                try {
                                    String data = thongDiepThat.substring(5).trim();
                                    String[] parts = data.split("\\s+");
                                    if (parts.length == 3) {
                                        String ip = parts[0];
                                        int startPort = Integer.parseInt(parts[1]);
                                        int endPort = Integer.parseInt(parts[2]);
                                        cauTraLoi = scanPort(ip, startPort, endPort);
                                    } else {
                                        cauTraLoi = "Sai cú pháp! Vui lòng nhập: PORT|192.168.1.1 1 1000";
                                    }
                                } catch (Exception e) {
                                    cauTraLoi = "Đã xảy ra lỗi khi quét port: " + e.getMessage();
                                }
                            }

                            // === IP ===
                            else if (thongDiepThat.toUpperCase().startsWith("IP|")) {
                                try {
                                    String ip = thongDiepThat.substring(3).trim();
                                    String urlString = "http://ip-api.com/json/" + ip + "?lang=vi";
                                    URL url = new URL(urlString);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("GET");

                                    BufferedReader inReader = new BufferedReader(
                                            new InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                                    String inputLine;
                                    StringBuilder responseData = new StringBuilder();
                                    while ((inputLine = inReader.readLine()) != null) {
                                        responseData.append(inputLine);
                                    }
                                    inReader.close();

                                    String jsonResponse = responseData.toString();
                                    if (jsonResponse.contains("\"status\":\"fail\"")) {
                                        cauTraLoi = "Lỗi: Không tìm thấy thông tin cho IP " + ip;
                                    } else {
                                        String country = jsonResponse.contains("\"country\":\"") ? jsonResponse.split("\"country\":\"")[1].split("\"")[0] : "Không rõ";
                                        String regionName = jsonResponse.contains("\"regionName\":\"") ? jsonResponse.split("\"regionName\":\"")[1].split("\"")[0] : "Không rõ";
                                        String city = jsonResponse.contains("\"city\":\"") ? jsonResponse.split("\"city\":\"")[1].split("\"")[0] : "Không rõ";
                                        String zip = jsonResponse.contains("\"zip\":\"") ? jsonResponse.split("\"zip\":\"")[1].split("\"")[0] : "Không rõ";
                                        String timezone = jsonResponse.contains("\"timezone\":\"") ? jsonResponse.split("\"timezone\":\"")[1].split("\"")[0] : "Không rõ";
                                        String isp = jsonResponse.contains("\"isp\":\"") ? jsonResponse.split("\"isp\":\"")[1].split("\"")[0] : "Không rõ";
                                        String org = jsonResponse.contains("\"org\":\"") ? jsonResponse.split("\"org\":\"")[1].split("\"")[0] : "Không rõ";
                                        String as = jsonResponse.contains("\"as\":\"") ? jsonResponse.split("\"as\":\"")[1].split("\"")[0] : "Không rõ";
                                        String lat = jsonResponse.contains("\"lat\":") ? jsonResponse.split("\"lat\":")[1].split(",")[0] : "Không rõ";
                                        String lon = jsonResponse.contains("\"lon\":") ? jsonResponse.split("\"lon\":")[1].split(",")[0] : "Không rõ";

                                        cauTraLoi = "THÔNG TIN CHI TIẾT IP: " + ip + "\n"
                                                + "Quốc gia: " + country + "\n"
                                                + "Tỉnh/Bang: " + regionName + "\n"
                                                + "Thành phố: " + city + "\n"
                                                + "Mã bưu chính: " + zip + "\n"
                                                + "Tọa độ: " + lat + ", " + lon + "\n"
                                                + "Múi giờ: " + timezone + "\n"
                                                + "---------------------------\n"
                                                + "Nhà mạng (ISP): " + isp + "\n"
                                                + "Tổ chức (Org): " + org + "\n"
                                                + "Số hiệu mạng: " + as;
                                    }
                                } catch (Exception e) {
                                    cauTraLoi = "Đã xảy ra lỗi khi tra cứu IP: " + e.getMessage();
                                }
                            } else {
                                cauTraLoi = "Server khong hieu lenh nay!";
                            }

                            String phanHoiMaHoa = AESUtil.encrypt(cauTraLoi);
                            out.writeUTF(phanHoiMaHoa);
                            System.out.println("Da gui cau tra loi cho Client!");
                        }

                    } catch (java.io.EOFException e) {
                        System.out.println("Client " + (loggedInUser != null ? loggedInUser : "") + " da ngat ket noi.");
                    } catch (Exception e) {
                        System.err.println("Loi o luong Client: " + e.getMessage());
                    } finally {
                        try {
                            if (socket != null && !socket.isClosed()) {
                                socket.close();
                            }
                        } catch (Exception ex) {
                        }
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
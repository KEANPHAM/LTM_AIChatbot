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
import java.util.Scanner;

public class Chatbot_Server {
    
    // Hàm gọi API Thời tiết (Dự báo nhiều ngày)
    public static String getWeather(String city) {
        try {
            String apiKey = "d9d9d1531734561bdc22bb8f6de37e7b"; 
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
// Hàm loại bỏ dấu tiếng Việt
    public static String removeAccents(String str) {
        try {
            String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
        } catch (Exception e) {
            return str;
        }
    }
    // Hàm gọi AI Groq
    // Hàm gọi AI Groq (Bản tối ưu không phụ thuộc thư viện ngoài)
    public static String goiAI(String cauHoi) {
        try {
            String apiKey = ""; 
            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + cauHoi.replace("\"", "\\\"") + "\"}"
                    + "]"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes("utf-8"));
            }

            if (conn.getResponseCode() != 200) {
                return "Groq API báo lỗi (Mã lỗi: " + conn.getResponseCode() + ").";
            }

            // Đọc luồng dữ liệu trả về
            try (Scanner sc = new Scanner(conn.getInputStream(), "utf-8")) {
                String response = sc.useDelimiter("\\A").next();
                
                // BÓC TÁCH CHUỖI THỦ CÔNG: Tìm nội dung nằm giữa trường "content":" và dấu đóng ngoặc kép "
                if (response.contains("\"content\":\"")) {
                    String content = response.split("\"content\":\"")[1];
                    // Tìm dấu ngoặc kép kết thúc của trường content (loại trừ các dấu ngoặc kép bị gạch chéo \" bên trong)
                    int endIdx = 0;
                    for (int i = 0; i < content.length(); i++) {
                        if (content.charAt(i) == '"' && content.charAt(i - 1) != '\\') {
                            endIdx = i;
                            break;
                        }
                    }
                    String ketQuaAI = content.substring(0, endIdx);
                    
                    // Xử lý hoàn trả các ký tự xuống dòng hoặc tab hiển thị cho đẹp
                    return ketQuaAI.replace("\\n", "\n").replace("\\\"", "\"");
                }
                
                return "Không thể phân tích câu trả lời từ AI.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối AI: " + e.getMessage();
        }
    }
    // Hàm Quét Port (Port Scanner)
    public static String scanPort(String ip, int startPort, int endPort) {
        StringBuilder openPorts = new StringBuilder("Các port đang mở trên " + ip + ": ");
        boolean found = false;

        if (endPort < startPort || (endPort - startPort > 1000)) {
            return "Lỗi: Dải port không hợp lệ hoặc quá lớn (Giới hạn tối đa 1000 port/lần quét).";
        }

        for (int port = startPort; port <= endPort; port++) {
            try {
                Socket socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(ip, port), 100);
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

    public static void main(String[] args) {
        try {
            System.out.println("Dang mo Port 8888, cho Client ket noi...");
            ServerSocket serverSocket = new ServerSocket(8888);
            
            while (true) {
                Socket socket = serverSocket.accept(); 
                System.out.println("Client da ket noi thanh cong!");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String messageTuClient = in.readUTF();
                System.out.println("Server nhan duoc chuoi ma hoa: " + messageTuClient);

                String thongDiepThat = AESUtil.decrypt(messageTuClient);
                System.out.println("Nhan duoc: [" + thongDiepThat + "]");
                System.out.println("Noi dung Client gui: " + thongDiepThat);

                String cauTraLoi = "";

               // === 1. XỬ LÝ LỆNH WEATHER ===
                if (thongDiepThat.toUpperCase().startsWith("WEATHER|")) {
                    System.out.println("DA VAO WEATHER");
                    String[] parts = thongDiepThat.split("\\|", 3);

                    String username = parts[1];
                    String tenThanhPho = parts[2];
                    String thanhPhoKhongDau = removeAccents(tenThanhPho);
                    System.out.println("Client muon xem thoi tiet o: " + thanhPhoKhongDau);
                    
                    String jsonThoiTiet = getWeather(thanhPhoKhongDau);

                    if(jsonThoiTiet.contains("Khong the lay thong tin")) {
                        cauTraLoi = "Lỗi: Không thể kết nối đến máy chủ thời tiết. Vui lòng kiểm tra lại!";
                    } else {
                        try {
                            org.json.JSONObject jsonObj = new org.json.JSONObject(jsonThoiTiet);
                            org.json.JSONArray danhSachDuBao = jsonObj.getJSONArray("list");
                            
                            // Đổi giao diện hiển thị: Không dùng emoji để tránh lỗi font Java Swing
                            StringBuilder sb = new StringBuilder();
                            sb.append("=== DỰ BÁO THỜI TIẾT: ").append(tenThanhPho.toUpperCase()).append(" (24H TỚI) ===\n");
                            
                            for (int i = 0; i < danhSachDuBao.length(); i++) {
                                org.json.JSONObject thoiDiem = danhSachDuBao.getJSONObject(i);
                                
                                // Chuỗi gốc: 2026-05-26 18:00:00
                                String thoiGianGoc = thoiDiem.getString("dt_txt"); 
                                
                                // Cắt chuỗi để format lại cho đẹp (Lấy ngày/tháng và Giờ:Phút)
                                String ngayThang = thoiGianGoc.substring(8, 10) + "/" + thoiGianGoc.substring(5, 7); // 26/05
                                String gioPhut = thoiGianGoc.substring(11, 16); // 18:00
                                
                                double nhietDo = thoiDiem.getJSONObject("main").getDouble("temp");
                                String moTa = thoiDiem.getJSONArray("weather").getJSONObject(0).getString("description");
                                
                                // Viết hoa chữ cái đầu của mô tả thời tiết cho đẹp
                                moTa = moTa.substring(0, 1).toUpperCase() + moTa.substring(1);
                                
                                // Ghép chuỗi theo định dạng: [18:00 - 26/05] Nhiệt độ: 31.1°C | Mây thưa
                                sb.append(String.format("[%s - %s] Nhiệt độ: %.1f°C | %s\n", gioPhut, ngayThang, nhietDo, moTa));
                            }
                            sb.append("------------------------------------------");
                            cauTraLoi = sb.toString();
                            
                        } catch (Exception e) {
                            cauTraLoi = "Lỗi khi bóc tách dữ liệu thời tiết: " + e.getMessage();
                        }
                    }
                }
                // === 2. XỬ LÝ LỆNH CHAT ===
                else if (thongDiepThat.toUpperCase().startsWith("CHAT|")) {
                   String[] parts = thongDiepThat.split("\\|", 3);

                    String username = parts[1];
                    String cauHoi = parts[2];

                    cauTraLoi = goiAI(cauHoi);
                } 
                // === 3. XỬ LÝ LỆNH PORT ===
                else if (thongDiepThat.toUpperCase().startsWith("PORT|")) {
                    try {
                        String[] parts = thongDiepThat.split("\\|", 3);

                        String username = parts[1];
                        String data = parts[2];
                                                
                        
                     String[] portParts = data.split("\\s+");

                    if (portParts.length == 3) {
                        String ip = portParts[0];
                        int startPort = Integer.parseInt(portParts[1]);
                        int endPort = Integer.parseInt(portParts[2]);

                        cauTraLoi = scanPort(ip, startPort, endPort);
                        } else {
                            cauTraLoi = "Sai cú pháp! Vui lòng nhập: PORT|192.168.1.1 1 1000";
                        }
                    } catch (NumberFormatException e) {
                        cauTraLoi = "Lỗi: Port bắt đầu và port kết thúc phải là chữ số nguyên!";
                    } catch (Exception e) {
                        cauTraLoi = "Đã xảy ra lỗi khi quét port: " + e.getMessage();
                    }
                } 
                // === 4. XỬ LÝ LỆNH IP ===
                else if (thongDiepThat.toUpperCase().startsWith("IP|")) {
                    try {
                        String[] parts = thongDiepThat.split("\\|", 3);

                        String username = parts[1];
                        String ip = parts[2];
                        String urlString = "http://ip-api.com/json/" + ip + "?lang=vi";
                        
                        java.net.URL url = new java.net.URL(urlString);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        
                        java.io.BufferedReader inReader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
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
                           // Trích xuất các trường dữ liệu mở rộng (Bóc tách thủ công)
                        String country = jsonResponse.contains("\"country\":\"") ? jsonResponse.split("\"country\":\"")[1].split("\"")[0] : "Không rõ";
                        String regionName = jsonResponse.contains("\"regionName\":\"") ? jsonResponse.split("\"regionName\":\"")[1].split("\"")[0] : "Không rõ";
                        String city = jsonResponse.contains("\"city\":\"") ? jsonResponse.split("\"city\":\"")[1].split("\"")[0] : "Không rõ";
                        String zip = jsonResponse.contains("\"zip\":\"") ? jsonResponse.split("\"zip\":\"")[1].split("\"")[0] : "Không rõ";
                        String timezone = jsonResponse.contains("\"timezone\":\"") ? jsonResponse.split("\"timezone\":\"")[1].split("\"")[0] : "Không rõ";
                        String isp = jsonResponse.contains("\"isp\":\"") ? jsonResponse.split("\"isp\":\"")[1].split("\"")[0] : "Không rõ";
                        String org = jsonResponse.contains("\"org\":\"") ? jsonResponse.split("\"org\":\"")[1].split("\"")[0] : "Không rõ";
                        String as = jsonResponse.contains("\"as\":\"") ? jsonResponse.split("\"as\":\"")[1].split("\"")[0] : "Không rõ";
                        
                        // lat và lon là số, không có dấu ngoặc kép trong JSON
                        String lat = jsonResponse.contains("\"lat\":") ? jsonResponse.split("\"lat\":")[1].split(",")[0] : "Không rõ";
                        String lon = jsonResponse.contains("\"lon\":") ? jsonResponse.split("\"lon\":")[1].split(",")[0] : "Không rõ";
                        
                        // 4. Gói ghém câu trả lời với giao diện "Premium"
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
                } 
                // === 5. KHÔNG HIỂU LỆNH ===
                else {
                    cauTraLoi = "Server khong hieu lenh nay!";
                }

                String phanHoiMaHoa = AESUtil.encrypt(cauTraLoi);
                out.writeUTF(phanHoiMaHoa);
                System.out.println("Da gui cau tra loi cho Client!");

                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
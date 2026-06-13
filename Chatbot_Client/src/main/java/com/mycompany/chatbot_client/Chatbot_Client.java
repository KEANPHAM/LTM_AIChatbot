/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatbot_client;

// Giữ nguyên dòng package của bạn ở đây
// package com.mycompany.chatb...;
import javax.swing.JOptionPane;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Chatbot_Client {
    public static void main(String[] args) {
        try {
          
            // 1. Gõ cửa địa chỉ IP của Server (localhost) tại Port 8888
            System.out.println("Dang ket noi den Server...");
            Socket socket = new Socket("localhost", 8888);
            System.out.println("Ket noi thanh cong!");

            // 2. Chuẩn bị luồng nhận/gửi dữ liệu
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

           
            // 3. Chuẩn bị tin nhắn gốc và MÃ HÓA nó
            String tinNhanGoc = "WEATHER|Hà Nội";
            String tinNhanDaMaHoa = AESUtil.encrypt(tinNhanGoc); // Gọi hàm mã hóa

            System.out.println("Tin nhan truoc khi gui (Da ma hoa): " + tinNhanDaMaHoa);

            // Gửi cục dữ liệu đã mã hóa đi
            out.writeUTF(tinNhanDaMaHoa);

            // 4. Nhận tin nhắn trả lời từ Server (Đang bị khóa)
            String responseFromServer = in.readUTF();

            // 5. MỞ KHÓA (Giải mã) tin nhắn
            String thongDiepThat = AESUtil.decrypt(responseFromServer);

            // 6. In ra thông điệp thật đã được giải mã
            JOptionPane.showMessageDialog(null, "Server response: " + thongDiepThat);

            // 7. Đóng kết nối
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
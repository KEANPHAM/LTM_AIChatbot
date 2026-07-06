# Topic 7: Xây dựng chương trình AI Chatbot


### ***Yêu cầu về chức năng phía client (phải có GUI):***

Sử dụng API của Simsimi, OpenAI (hoặc tự chọn lựa một công cụ khác) để xây dựng Al chatbot. Đồng thời với chat tự động, chatbot sẽ trả lời thông tin thời tiết, xác định vị trí IP, quét port khi người dùng đặt câu hỏi, chỉ tiết như sau:

- Tra cứu thời tiết: gửi yêu cầu là tên một tỉnh/thành phố hoặc địa danh (bằng tiếng Việt). Kết quả phản hồi là thời tiết ngày hiện tại và một số ngày/tuần (do SV quyết định) kế tiếp của địa điểm đó.
- Xác định vị trí IP: gửi yêu cầu là 1 địa chỉ IP bất kỳ. Kết quả phản hồi là tọa độ địa điểm tương ứng với địa chỉ IP đó. SV tham khảo kết quả từ https://www.iplocation.net/de biết các thông tin cần trả về. Nhóm SV có thể sử dụng API có sẵn hoặc trích xuất kết quả từ một website có công cụ này.
- Quét port: gửi yêu cầu là một địa chỉ IP. Kết quả phản hồi là các port đang mở trong giới hạn từ port x đến port y (với x, y là dữ liệu người dùng nhập).
- Các chức năng khác nếu có cài đặt sẽ có điểm cộng.

### *Yêu cầu về chức năng phía server (không cần GUI):*

- Nhân các yêu cầu từ client. sử dụng Java socket kết hợp API hoặc (các tools/ngôn ngữ khác) để lấy kết quả trả về cho client. Tất cả xử lý phải nằm ở phía server.
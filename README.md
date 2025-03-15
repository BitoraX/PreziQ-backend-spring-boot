# ** PriziQ! - Nền tảng học tập tương tác theo mô hình Game-Based Learning **

Ứng dụng Java Spring Boot kết hợp PostgreSQL, Maven, STOMP, Docker,...

---

## **Yêu cầu hệ thống**

Trước khi chạy dự án, hãy đảm bảo bạn đã cài đặt các công cụ sau:

1. **Java Development Kit (JDK)**
    - Phiên bản: 21 hoặc cao hơn
    - [Tải JDK tại đây](https://www.oracle.com/java/technologies/javase-downloads.html)

2. **Apache Maven**
    - Phiên bản: 3.8.6 hoặc cao hơn
    - [Tải Maven tại đây](https://maven.apache.org/download.cgi)

3. **Cơ sở dữ liệu PostgreSQL**
    - Đảm bảo PostgreSQL đã được cài đặt và đang chạy.
    - Tạo cơ sở dữ liệu cho dự án (mặc định: `priziq`).

4. **Git**
    - [Tải Git tại đây](https://git-scm.com/downloads)

5. **IDE (không bắt buộc)**
    - IntelliJ IDEA hoặc Eclipse để phát triển dễ dàng hơn.

---

## **Hướng dẫn cài đặt và chạy**

### **Bước 1: Clone dự án**

Clone dự án về máy:
```bash
git clone https://github.com/BitoraX/PreziQ-backend-spring-boot.git
cd PreziQ-backend-spring-boot
```

### **Bước 2: Cấu hình cơ sở dữ liệu**

1. Mở file `src/main/resources/application.yml`.
2. Cập nhật thông tin kết nối cơ sở dữ liệu PostgreSQL:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/priziq
       username: tên_đăng_nhập_postgresql
       password: mật_khẩu_postgresql
   ```
3. Tạo cơ sở dữ liệu nếu chưa có:
   ```sql
   CREATE DATABASE priziq;
   ```

### **Bước 3: Build dự án**

Sử dụng Maven để build dự án:
```bash
mvn clean install
```

### **Bước 4: Chạy ứng dụng**

Chạy ứng dụng bằng Maven:
```bash
mvn spring-boot:run
```

Hoặc chạy file JAR đã được build:
```bash
java -jar target/priziq-0.0.1-SNAPSHOT.jar
```

---

## **Truy cập ứng dụng**

1. Mở trình duyệt và truy cập:
   ```
   http://localhost:8080
   ```

2. Các API sẽ được cung cấp tại `/api/v1`. Ví dụ:
    - Đăng nhập: `POST /api/v1/auth/login`

---

## **Kiểm tra ứng dụng**

### **Bước 1: Chạy kiểm tra**

Để chạy các bài kiểm tra (unit tests và integration tests), thực hiện lệnh:
```bash
mvn test
```

---

## **Format Response**

### **1. Thành công**

```json
{
  "code": 1000,
  "statusCode": 200,
  "message": "Yêu cầu xử lý thành công",
  "data": {
    "id": "12345",
    "name": "Quách Phú Thuận",
    "email": "thuanflu@example.com"
  },
  "timestamp": "2024-12-28T15:00:00Z",
  "path": "/api/v1/auth/login"
}
```

- **`code`**: Mã trạng thái nội bộ của hệ thống.
- **`statusCode`**: Mã HTTP tiêu chuẩn.
- **`message`**: Mô tả ngắn gọn về trạng thái thành công của request.
- **`data`**: Payload chứa dữ liệu trả về.
- **`timestamp`**: Thời gian phản hồi.
- **`path`**: API đã yêu cầu thành công.

### **2. Lỗi**

```json
{
   "code": 1003,
   "statusCode": 400,
   "message": "Dữ liệu trong yêu cầu không hợp lệ. Vui lòng kiểm tra và thử lại",
   "errors": [
      {
         "code": 1001,
         "message": "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt"
      },
      {
         "code": 1002,
         "message": "Tên phải là chuỗi ký tự"
      }
   ],
   "timestamp": "2025-01-26T03:50:52.555Z",
   "path": "/api/v1/auth/register"
}
```

- **`statusCode`**: Mã HTTP tiêu chuẩn.
- **`errors`**: Danh sách toàn bộ lỗi.
- **`code`**: Mã trạng thái nội bộ của hệ thống.
- **`message`**: Mô tả ngắn gọn về trạng thái lỗi của request.
- **`timestamp`**: Thời gian lỗi xảy ra.
- **`path`**: API gây lỗi.

---

## **Các lỗi phổ biến và cách khắc phục**

### **Lỗi: "Port 8080 is already in use"**
- **Cách khắc phục:** Thay đổi cổng trong `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

### **Lỗi: "Database Connection Error"**
- **Cách khắc phục:** Đảm bảo PostgreSQL đang chạy và kiểm tra lại thông tin kết nối trong `application.yml`.

### **Lỗi: "Maven Build Fails"**
- **Cách khắc phục:** Kiểm tra Maven đã được cài đặt và thêm vào PATH và thực hiện lại lệnh `mvn clean install`.

---

## **Hướng dẫn đóng góp**

1. Fork repository.
2. Tạo branch mới:
   ```bash
   git checkout -b feature/ten-tinh-nang
   ```
3. Commit các thay đổi:
   ```bash
   git commit -m "Thêm mô tả commit tại đây"
   ```
4. Push branch lên repository của bạn:
   ```bash
   git push origin feature/ten-tinh-nang
   ```
5. Tạo pull request.

---

## **Giấy phép**

Dự án này được cấp phép theo giấy phép MIT. Xem chi tiết trong file `LICENSE`.

---

## **Liên hệ**

Nếu bạn có câu hỏi hoặc cần hỗ trợ, vui lòng liên hệ:
- **Email:** support@bitorax.com
- **GitHub Issues:** [Tạo một Issue](https://github.com/BitoraX/PreziQ-backend-spring-boot/issues)

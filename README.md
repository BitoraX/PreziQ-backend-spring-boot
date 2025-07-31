# PreziQ! - Nền tảng học tập tương tác theo mô hình Game-Based Learning

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
   - Tạo cơ sở dữ liệu cho dự án (mặc định: `preziq`).

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
       url: jdbc:postgresql://localhost:5432/preziq
       username: tên_đăng_nhập_postgresql
       password: mật_khẩu_postgresql
   ```
3. Tạo cơ sở dữ liệu nếu chưa có:
   ```sql
   CREATE DATABASE preziq;
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
java -jar target/preziq-0.0.1-SNAPSHOT.jar
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
  "success": true,
  "message": "Request processed successfully",
  "data": {
    "id": "12345",
    "name": "Quách Phú Thuận",
    "email": "thuanflu@example.com"
  },
  "meta": {
    "timestamp": "2024-12-28T15:00:00Z",
    "instance": "/api/v1/auth/login"
  }
}
```

### **2. Lỗi (Validation / DTO Error)**

```json
{
  "success": false,
  "errors": [
    {
      "resource": "user",
      "field": "email",
      "code": 2000,
      "message": "Email has already been taken"
    },
    {
      "resource": "user",
      "field": "password",
      "code": 2001,
      "message": "Password must be at least 8 characters"
    }
  ],
  "meta": {
    "timestamp": "2025-01-26T03:50:52.555Z",
    "instance": "/api/v1/auth/register"
  }
}
```

### **3. Lỗi nghiệp vụ từ Service / Filter**

```json
{
  "success": false,
  "errors": [
    {
      "code": 1002,
      "message": "Cannot update this record"
    }
  ],
  "meta": {
    "timestamp": "2025-01-26T03:50:52.555Z",
    "instance": "/api/v1/resource/123"
  }
}
```

### **Giải thích các trường trong response:**

- `success`: Boolean, xác định request thành công hay thất bại.
- `message`: Mô tả ngắn gọn khi `success = true`, dùng cho thông báo frontend.
- `data`: Payload trả về từ server khi request thành công.
- `errors`: Danh sách lỗi trả về khi request thất bại. Có thể là lỗi DTO hoặc lỗi service.
   - `resource`: Tên entity bị lỗi (chỉ áp dụng với lỗi DTO).
   - `field`: Tên trường cụ thể gây lỗi (chỉ áp dụng với lỗi DTO).
   - `code`: Mã lỗi nội bộ giúp frontend xử lý logic.
   - `message`: Mô tả chi tiết lỗi để hiển thị cho người dùng hoặc debug.
- `meta`: Thông tin bổ sung cho phản hồi.
   - `timestamp`: Thời điểm server xử lý response (ISO-8601).
   - `instance`: API endpoint tương ứng với request.

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
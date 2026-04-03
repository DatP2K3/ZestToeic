# ZestTOEIC Backend API

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/ZestTOEIC/zesttoeic-api)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://jdk.java.net/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5+-green.svg)](https://spring.io/projects/spring-boot)

Hệ thống ZestTOEIC Backend API - Nền tảng luyện thi TOEIC kết hợp yếu tố Gamification và Trí tuệ Nhân tạo (AI).

## 1. Yêu cầu Hệ thống
- **JDK 21** trở lên.
- **MongoDB** đang chạy ở port mặc định `27017`.
- **RabbitMQ** đang chạy ở port `5672` (Hoặc dùng Docker: `docker-compose up -d`).
- **Redis** ở port `6379`.

## 2. Các Modules Cốt Lõi
- `auth`: Xác thực & Ủy quyền bằng JWT, phân quyền Premium.
- `practice`: Core luyện tập, Box flashcards (SRS Leitner) và cấu trúc thi TOEIC.
- `gamification`: Hệ thống danh hiệu (Badges), chuỗi ngày học (Streaks) và Exp kinh nghiệm.
- `community`: Cộng đồng học tập, Diễn đàn & Bạn bè.
- `battle`: Real-time WebSocket PvP Battle Royale cho người thi.
- `intelligence`: Gemini AI phân tích điểm yếu và sinh câu hỏi bổ sung.

## 3. Chạy Dự án Cục bộ (Local)

**Bước 1:** Bật các dependencies (DB, Message Queue) qua Docker.
```bash
docker-compose up -d
```

**Bước 2:** Đảm bảo thay đổi các API keys bắt buộc trong `application.yaml` hoặc `.env`:
- `GOOGLE_GEMINI_API_KEY`: API key của Google Gemini.
- `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`: (Tuỳ chọn cho thanh toán).
- `JWT_SECRET`: Chuỗi khóa ký Token dài ít nhất 32 bytes (256 bits).

**Bước 3:** Chạy server
```bash
./mvnw spring-boot:run
```
Máy chủ sẽ mặc định lắng nghe tại cổng `8080`. API Docs có sẵn tại: `http://localhost:8080/swagger-ui/index.html`

## 4. Chạy Unit & Integration Tests
Dự án có độ phủ test toàn diện qua bộ khung JUnit 5, Mockito và Spring Boot Test.
Để chạy sạch toàn bộ Tests (Sẽ tự validate cả ArchUnit rule):
```bash
./mvnw clean test
```

## 5. Kiến Trúc Cơ Sở
Đọc thêm tài liệu về cấu trúc thiết kế trong hệ mục [docs/03-architecture](docs/03-architecture).
Đặc biệt, hệ thống sử dụng:
1. **Domain Events** cho tương tác đa module để đảm bảo decoupling.
2. **Resilience4j** Circuit Breakers bảo vệ AI integration.
3. Không sử dụng Raw Map/Object mà đảm bảo Strict Type 100% bằng Java Record / DTO.

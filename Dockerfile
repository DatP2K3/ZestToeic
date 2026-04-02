FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy tệp thực thi jar được build từ Maven vào trong Docker Container
COPY target/zesttoeic-api-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng giao tiếp mặc định của Spring Boot
EXPOSE 8080

# Cấu hình biến môi trường chuẩn (tuỳ chọn)
ENV TZ=Asia/Ho_Chi_Minh

# Lệnh khởi động 
ENTRYPOINT ["java", "-jar", "app.jar"]

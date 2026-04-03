# Stage 1: Build the application with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy source code and build (No local Java required)
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests && mv target/*.jar target/app.jar

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /build/target/app.jar app.jar

# Mở cổng giao tiếp mặc định của Spring Boot
EXPOSE 8080

# Cấu hình biến môi trường chuẩn (tuỳ chọn)
ENV TZ=Asia/Ho_Chi_Minh

# Lệnh khởi động 
ENTRYPOINT ["java", "-jar", "app.jar"]

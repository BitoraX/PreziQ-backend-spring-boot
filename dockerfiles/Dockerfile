# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -T 1C --no-transfer-progress

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/PriziQ-0.0.1-SNAPSHOT.jar priziq.jar
COPY .env .env
ENV SPRING_CONFIG_IMPORT=file:/app/.env
EXPOSE 8080
CMD ["java", "-jar", "priziq.jar"]
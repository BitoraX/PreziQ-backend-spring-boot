# Stage 1: Build the application using Maven and JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B # Load all libraries (dependencies) declared in pom.xml and cache
COPY src ./src
RUN mvn clean package -DskipTests # Compile the source code and package the application into a .jar file

# Stage 2: Run the built application using a lightweight JDK 21 runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Set timezone to Asia/Ho_Chi_Minh (GMT+7)
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
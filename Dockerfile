FROM openjdk:17-jdk-slim

LABEL maintainer="efojunior25@gmail.com"
LABEL description="XunimPay Payment System"

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Run application
CMD ["java", "-jar", "target/payment-system-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=docker"]
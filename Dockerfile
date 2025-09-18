FROM openjdk:17-jdk-slim

LABEL maintainer="efojunior25@gmail.com"
LABEL description="XunimPay Payment System"
LABEL version="1.0.0"

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dar permissão de execução ao mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Health check usando o endpoint simples
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health/simple || exit 1

# Run application with optimized JVM settings
CMD ["java", \
     "-Xms256m", \
     "-Xmx512m", \
     "-XX:+UseG1GC", \
     "-XX:+UseContainerSupport", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "target/payment-system-0.0.1-SNAPSHOT.jar", \
     "--spring.profiles.active=docker"]
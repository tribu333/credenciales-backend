# Etapa 1: Construcción - Usando imagen oficial de Gradle con Eclipse Temurin JDK 17
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copiar archivos de configuración
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle

# Dar permisos de ejecución al wrapper
RUN chmod +x gradlew

# Copiar código fuente
COPY src ./src

# Construir la aplicación (excluyendo tests para imagen más rápida)
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: Imagen final - Usando Eclipse Temurin JDK 17 (recomendado por Spring)
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Crear directorio para uploads (importante para tu app)
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/build/libs/*.jar app.jar

# Puerto que usa tu aplicación (8084)
EXPOSE 8085

# Variables de entorno que se pueden sobrescribir en Render
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8085 \
    FILE_UPLOAD_DIR=/app/uploads

# Health check para Render
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD java -jar app.jar --health || exit 1

# Ejecutar como usuario no root (más seguro)
RUN groupadd --system javauser && useradd --system --gid javauser javauser
USER javauser:javauser

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
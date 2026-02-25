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

# Instalar curl para healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
# Construir la aplicación (excluyendo tests para imagen más rápida)
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: Imagen final - Usando Eclipse Temurin JDK 17 (recomendado por Spring)
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app
#este expose no lo puse al despliege
#EXPOSE 8080

# Crear directorio para uploads (importante para tu app)
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Crear el directorio y dar permisos antes de copiar la app
RUN mkdir -p /app/imagenes_subidas && \
    chmod 777 /app/imagenes_subidas

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/build/libs/*.jar app.jar

# Ejecutar como usuario no root (más seguro)
RUN groupadd --system javauser && useradd --system --gid javauser javauser
USER javauser:javauser

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
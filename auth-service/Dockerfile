# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1️⃣ Copiamos solo pom.xml para cachear dependencias
COPY pom.xml .

# 2️⃣ Descargamos todas las dependencias offline (cacheable)
RUN mvn dependency:go-offline

# 3️⃣ Copiamos el código fuente
COPY src ./src

# 4️⃣ Build en modo "layered jar", saltando tests
RUN mvn clean package -DskipTests \
    -Dspring-boot.build-image.skip=true \
    -Dspring-boot.repackage.skip=false

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto de la app
EXPOSE 8082

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]
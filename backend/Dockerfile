# ===============================
# BUILD STAGE
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src src
RUN mvn clean package -DskipTests


# ===============================
# RUNTIME STAGE
# ===============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/payment-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
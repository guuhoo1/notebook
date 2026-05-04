FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY server/pom.xml .
COPY server/src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    apk del maven

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/server/target/*.jar app.jar

RUN apk add --no-cache bash && \
    mkdir -p /app/uploads && \
    chmod 755 /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]

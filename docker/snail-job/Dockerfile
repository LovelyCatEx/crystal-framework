FROM maven:3.8.6-eclipse-temurin-17 AS builder

RUN apt-get update && apt-get install -y --no-install-recommends unzip

WORKDIR /build

COPY snail-job-master.zip /build/

RUN unzip -q snail-job-master.zip

WORKDIR /build/snail-job-master

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/snail-job-master/snail-job-server-starter/target/snail-job-server-exec.jar /app/snail-job-server.jar

RUN mkdir -p /app/config

EXPOSE 17888

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:17888/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java -jar /app/snail-job-server.jar --spring.config.location=file:/app/config/application.yaml"]
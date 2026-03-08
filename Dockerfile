FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS dependencies

WORKDIR /app

RUN apk add --no-cache git

COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw mvnw

RUN chmod +x mvnw

RUN git clone https://github.com/LovelyCatEx/VertexLib.git /tmp/VertexLib && \
    cd /tmp/VertexLib && \
    /app/mvnw install -DskipTests

RUN ./mvnw dependency:go-offline -B

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY --from=dependencies /app/.mvn ./.mvn
COPY --from=dependencies /app/mvnw .
COPY --from=dependencies /app/pom.xml .
COPY --from=dependencies /root/.m2 /root/.m2

COPY src/ src/

RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
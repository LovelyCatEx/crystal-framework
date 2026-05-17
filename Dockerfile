FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS dependencies

WORKDIR /dependencies

RUN apk add --no-cache git

COPY .mvn/ .mvn
COPY mvnw mvnw

RUN chmod +x mvnw

RUN git clone https://github.com/LovelyCatEx/VertexLib.git /tmp/VertexLib && \
    cd /tmp/VertexLib && \
    /dependencies/mvnw install -DskipTests

COPY . .

RUN ./mvnw dependency:go-offline -B

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY --from=dependencies /dependencies/ ./

RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/crystal-starter/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
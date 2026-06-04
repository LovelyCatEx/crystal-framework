FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS dependencies

WORKDIR /dependencies

RUN apk add --no-cache git

COPY .mvn/ .mvn
COPY mvnw mvnw

RUN chmod +x mvnw

RUN git clone https://github.com/LovelyCatEx/VertexLib.git /tmp/VertexLib && \
    cd /tmp/VertexLib && \
    /dependencies/mvnw install -DskipTests

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY --from=dependencies /dependencies/ ./
COPY --from=dependencies /root/.m2 /root/.m2

COPY . .

RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/crystal-starter/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar \"$@\"", "--"]
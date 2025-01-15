FROM maven:3.8.3-amazoncorretto-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
COPY config ./config

RUN mkdir -p /app/models
COPY src/main/resources/models_pbuid=test-pbuid.onnx /app/models/model.onnx

RUN mvn clean package -DskipTests

FROM amazoncorretto:17
WORKDIR /app

COPY --from=builder /app/target/java-local-debug2-1.0-SNAPSHOT.jar app.jar
COPY --from=builder /app/models/model.onnx /app/models/model.onnx

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
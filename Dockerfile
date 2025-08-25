FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/target/kafka-boilerplate-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Use environment variables with defaults
ENV KAFKA_BOOTSTRAP_SERVERS=d2gphehmodb6qsnjf2l0.any.ap-south-1.mpx.prd.cloud.redpanda.com:9092
ENV KAFKA_SECURITY_PROTOCOL=SASL_SSL
ENV KAFKA_SASL_MECHANISM=SCRAM-SHA-256
ENV KAFKA_USERNAME=mahabub637
ENV KAFKA_PASSWORD=aduQqonUEgOoKKdFmhM4hyGgxUoA1C
ENV ADVICE_TOPIC=advice-topic
ENV WIKIMEDIA_TOPIC=wikimedia
ENV WEBHOOK_TOPIC=event
ENV WIKIMEDIA_STREAM_URL=https://stream.wikimedia.org/v2/stream/recentchange

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
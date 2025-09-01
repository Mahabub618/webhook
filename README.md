# Kafka Boilerplate

A simple Spring Boot application for demonstrating Kafka integration.

## Features

- Spring Boot 3.5.4
- Kafka integration using `spring-kafka`
- REST API for sending messages
- JSON serialization/deserialization with Jackson

## Prerequisites

- Java 17
- Maven 3.8+
- Kafka broker running locally or remotely

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/mahabub618/kafka-boilerplate.git
cd kafka-boilerplate
```

## Docker Build and Run
```bash
docker build -t username/kafka-boilerplate:latest .
using specific version [docker build -t mahabub637/webhook:0.2.6 .]
docker run -d -p 8080:8080 --name kafka-boilerplate username/kafka-boilerplate:latest
```

## Docker push
```bash
docker push username/kafka-boilerplate:latest
using specific version [docker push mahabub637/webhook:0.2.6]
```
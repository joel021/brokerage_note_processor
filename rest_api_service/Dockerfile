FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn install -Pprod -DskipTests

FROM openjdk:17

WORKDIR /app

COPY --from=builder /app/target/stockprice.jar .

EXPOSE 8080

CMD ["java", "-jar", "stockprice.jar"]
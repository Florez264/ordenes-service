FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp verify
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-Xms32m -Xmx192m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -XX:ActiveProcessorCount=2 -XX:+ExitOnOutOfMemoryError"
COPY --from=build /app/target/ordenes-service-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

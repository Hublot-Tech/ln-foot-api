FROM eclipse-temurin:17-jdk-alpine

# JVM optimization for 4GB VPS
ENV JAVA_OPTS="-Xmx512m -Xms256m"

WORKDIR /app
COPY target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

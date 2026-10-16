FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY services/menu-service/pom.xml ./pom.xml
COPY services/menu-service/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/target/menu-service-0.0.1-SNAPSHOT.jar app.jar
USER spring:spring
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

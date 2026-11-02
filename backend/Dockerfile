FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY backend/pom.xml backend/
COPY backend backend
RUN mvn -f backend/pom.xml -DskipTests package -B -q

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/backend/target/voa-*.jar /app/voa.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/voa.jar"]

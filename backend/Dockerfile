FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
ENV MAVEN_OPTS="-Xms64m -Xmx512m"
COPY backend/pom.xml backend/
COPY backend backend
RUN mvn -f backend/pom.xml -DskipTests package -B -q

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/backend/target/voa-*.jar /app/voa.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Xms64m", "-Xmx512m", "-jar", "/app/voa.jar"]

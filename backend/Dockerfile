FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY backend/pom.xml backend/mvnw backend/.mvn/ backend/
RUN chmod +x backend/mvnw
RUN ./backend/mvnw -f backend/pom.xml dependency:go-offline
COPY backend backend
RUN ./backend/mvnw -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/backend/target/voa-*.jar /app/voa.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/voa.jar"]

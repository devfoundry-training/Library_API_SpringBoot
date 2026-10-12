# Library API (Spring Boot): build the jar with the Maven wrapper, then run it on a JRE as a non-root user.
#   docker build -t library-api-springboot .
#   docker run -p 8000:8000 library-api-springboot

# ---- build stage ----
FROM eclipse-temurin:21.0.12.1_1-jdk-noble AS build
WORKDIR /src

# Dependencies first, so a code change does not download them again.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:21.0.12.1_1-jre-alpine

# Time zone data for Europe/London, and a user without root rights.
RUN apk add --no-cache tzdata \
    && addgroup -S library \
    && adduser -S -G library -h /app library

WORKDIR /app
COPY --from=build /src/target/library-api.jar app.jar
RUN chown library:library /app

USER library
ENV PORT=8000 \
    TZ=Europe/London
EXPOSE 8000

# Flyway runs the migrations on start-up, then the sample data is loaded if the database is empty.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

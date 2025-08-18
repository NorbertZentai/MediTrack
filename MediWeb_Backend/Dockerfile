## Backend Docker image (single-stage for now; multi-stage optimization is a later step)
FROM eclipse-temurin:17-jdk

WORKDIR /app

## Install Maven (used only at build time inside this image)
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

## Copy project sources
COPY . .

## Build (tests skipped here; run tests in CI before building the image)
RUN mvn -q clean package -DskipTests

EXPOSE 8080

## Allow runtime JVM / Spring tuning via JAVA_OPTS env
ENV JAVA_OPTS=""

## Runtime: rely on environment variables (SPRING_PROFILES_ACTIVE, SPRING_DATASOURCE_*, etc.)
## Secrets MUST NOT be hardcoded in the image. Provide them at run/deploy time.
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
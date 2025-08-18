############### BUILD STAGE ###############
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom first for better dependency layer caching
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy sources
COPY src ./src

# Build application (skip tests - run them in CI beforehand)
RUN mvn -q clean package -DskipTests

############### RUNTIME STAGE ###############
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Add non-root user for security
RUN useradd -u 10001 -m spring

# Copy built artifact only
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# JVM / Spring tuning via JAVA_OPTS, allow profile override
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod

USER spring

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
# Stage 1: Build the application
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skipping tests for speed, remove -DskipTests if needed)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:11-jre
WORKDIR /app

# Copy the compiled .jar from the build stage
# Note: Adjust the jar name if it differs in your pom.xml
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

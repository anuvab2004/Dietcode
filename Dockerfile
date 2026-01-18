# Stage 1: Build the application
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# CHANGE: Switch from -jre to -jdk
FROM eclipse-temurin:17-jdk 
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Important for Render: The compiler needs to be in the system path, 
# which eclipse-temurin-jdk handles automatically.
ENTRYPOINT ["java", "-jar", "app.jar"]

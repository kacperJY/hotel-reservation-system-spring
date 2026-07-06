FROM eclipse-temurin:25-alpine as maven-build

COPY .mvn /build/.mvn
COPY mvnw pom.xml /build/
WORKDIR /build
RUN ./mvnw dependency:go-offline
COPY src /build/src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-alpine AS application
COPY --from=maven-build /build/target/hotel*.jar /opt/app.jar
WORKDIR /opt
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]
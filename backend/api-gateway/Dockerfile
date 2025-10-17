FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/api-gateway-*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

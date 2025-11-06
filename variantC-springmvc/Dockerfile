FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/variantC-springmvc-1.0.0.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

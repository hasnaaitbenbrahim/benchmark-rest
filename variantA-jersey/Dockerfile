FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/variantA-jersey-1.0.0.jar
ARG JMX_EXPORTER_VERSION=0.20.0
WORKDIR /app
# Download JMX Exporter agent
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${JMX_EXPORTER_VERSION}.jar /app/jmx_prometheus_javaagent.jar
COPY ${JAR_FILE} app.jar
COPY jmx-exporter-config.yml /app/jmx-exporter-config.yml
EXPOSE 8080 9404
ENTRYPOINT ["java","-javaagent:/app/jmx_prometheus_javaagent.jar=9404:/app/jmx-exporter-config.yml","-jar","/app/app.jar"]

FROM openjdk:21
ARG JAR_FILE=config-server/target/config-server.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar", "app.jar"]
EXPOSE 8888

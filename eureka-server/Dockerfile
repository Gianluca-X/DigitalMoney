FROM openjdk:21
ARG JAR_FILE=eureka-server/target/eureka-server.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8761


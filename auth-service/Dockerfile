FROM openjdk:21
ARG JAR_FILE=auth-service/target/auth-service.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8082

FROM openjdk:21
ARG JAR_FILE=user-service/target/user-service.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8087

FROM openjdk:17-oracle
COPY target/*.jar bot-service.jar
ENTRYPOINT ["java","-jar","/bot-service.jar"]
FROM openjdk:17-oracle
COPY target/*.jar tgbot_documents.jar
ENTRYPOINT ["java","-jar","/tgbot_documents.jar"]
ADD ./resources/schema.sql /docker-entrypoint-initdb.d/
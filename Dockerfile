FROM openjdk:17-oracle
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081
COPY target/*.jar bot-service.jar
ENTRYPOINT ["java","-jar","/bot-service.jar"]
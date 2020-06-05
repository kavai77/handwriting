FROM openjdk:11-jre
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/app.jar

ENTRYPOINT ["/usr/bin/java","-jar","/usr/share/app.jar"]

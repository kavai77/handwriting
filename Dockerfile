FROM adoptopenjdk/openjdk11-openj9:alpine
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/app.jar
RUN apk --no-cache add curl
ENTRYPOINT ["java","-Xshareclasses:cacheDir=/opt/shareclasses","-jar","/usr/share/app.jar"]

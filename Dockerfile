FROM openjdk:8-jdk-alpine

ARG JAR_FILE=g4hab-0.1.war
ARG JAR_LIB_FILE=/
ARG PORT=8080

# JAR_LIB_FILE is not used...

# WORKDIR /

ADD g4hab-0.1.war g4hab-0.1.war

EXPOSE ${PORT}

#ENTRYPOINT ["java", "-Dgrails.env=prod", "-Dserver.port=8080", "-jar", "g4mgd-0.1.war"]
#ENTRYPOINT ["/usr/bin/java","-jar","-Dspring.profiles.active=default","g4hab-0.1.war"]
#ARG JAVA_OPTS=-Xmx384m -Xss512k -XX:+UseCompressedOops
#${JAVA_OPTS}

CMD java -Dgrails.env=prod -Dserver.port=${PORT} -jar g4hab-0.1.war



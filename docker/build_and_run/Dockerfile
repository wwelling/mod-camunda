# build base image
FROM openjdk:17-slim as maven

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir /mod-camunda
WORKDIR /mod-camunda

# copy required files
COPY ./pom.xml ./pom.xml
COPY ./src ./src
COPY ./descriptors ./descriptors

# build
RUN mvn package

# final base image
FROM openjdk:17-slim

# Upgrade to latest patch versions of packages: https://pythonspeed.com/articles/security-updates-in-docker/
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y maven && \
    apt-get clean

# set deployment directory
WORKDIR /mod-camunda

# copy over the built artifact from the maven image
COPY --from=maven /mod-camunda/target/mod-camunda*.jar ./mod-camunda.jar

# environment
ENV SERVER_PORT='9000'

# expose port
EXPOSE ${SERVER_PORT}

# run java command
CMD java -jar -Xmx4096m ./mod-camunda.jar

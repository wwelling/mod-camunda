# build base image
FROM maven:3-jdk-8-alpine as maven

# copy pom.xml
COPY ./pom.xml ./pom.xml

# copy src
COPY ./src ./src

# add curl and git
RUN apk add --no-cache curl git

# clone master of mod-workflow
RUN git clone https://github.com/TAMULib/mod-workflow.git

WORKDIR /mod-workflow

# install mod-workflow components
RUN mvn install

WORKDIR /

# build
RUN mvn package

# final base image
FROM openjdk:8u171-jre-alpine

# set deployment directory
WORKDIR /mod-camunda

# copy over the built artifact from the maven image
COPY --from=maven /target/mod-camunda*.jar ./mod-camunda.jar

# settings
ENV LOGGING_LEVEL_FOLIO='INFO'
ENV SERVER_PORT='9000'
ENV SPRING_ACTIVEMQ_BROKER_URL='http://localhost:61616'
ENV SPRING_DATASOURCE_PLATFORM='h2'
ENV SPRING_DATASOURCE_URL='jdbc:h2:./mod-camunda;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
ENV SPRING_DATASOURCE_DRIVERCLASSNAME='org.h2.Driver'
ENV SPRING_DATASOURCE_USERNAME='folio'
ENV SPRING_DATASOURCE_PASSWORD='folio'
ENV SPRING_H2_CONSOLE_ENABLED='true'
ENV SPRING_H2_WEBALLOW='true'
ENV SPRING_JPA_DATABASE_PLATFORM='org.hibernate.dialect.H2Dialect'
ENV SPRING_ACTIVEMQ_USER='admin'
ENV SPRING_ACTIVEMQ_PASSWORD='admin'
ENV CAMUNDA_BPM_ADMIN_USER_ID='admin'
ENV CAMUNDA_BPM_ADMIN_USER_PASSWORD='admin'
ENV CAMUNDA_BPM_ADMIN_USER_FIRST_NAME='Camunda'
ENV CAMUNDA_BPM_ADMIN_USER_LAST_NAME='Admin'
ENV CAMUNDA_BPM_ADMIN_USER_EMAIL='cadmin@mailinator.com'
ENV EVENT_QUEUE_NAME='event.queue'
ENV TENANT_DEFAULT_TENANT='tern'
ENV TENANT_INITIALIZE_DEFAULT_TENANT='false'
ENV OKAPI_LOCATION='http://localhost:9130'
ENV OKAPI_USERNAME='tern_admin'
ENV OKAPI_PASSWORD='admin'

#expose port
EXPOSE ${SERVER_PORT}

#run java command
CMD java -jar -Xmx4096m ./mod-camunda.jar \
  --logging.level.org.folio=${LOGGING_LEVEL_FOLIO} --server.port=${SERVER_PORT} --spring.activemq.broker-url=${SPRING_ACTIVEMQ_BROKER_URL} \
  --spring.datasource.platform=${SPRING_DATASOURCE_PLATFORM} --spring.datasource.url=${SPRING_DATASOURCE_URL} \
  --spring.datasource.driverClassName=${SPRING_DATASOURCE_DRIVERCLASSNAME} --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} --spring.h2.console.enabled=${SPRING_H2_CONSOLE_ENABLED} \
  --spring.jpa.database-platform=${SPRING_JPA_DATABASE_PLATFORM} --camunda.bpm.admin-user.id=${CAMUNDA_BPM_ADMIN_USER_ID} \
  --camunda.bpm.admin-user.password=${CAMUNDA_BPM_ADMIN_USER_PASSWORD} --camunda.bpm.admin-user.first-name=${CAMUNDA_BPM_ADMIN_USER_FIRST_NAME} \
  --camunda.bpm.admin-user.last-name=${CAMUNDA_BPM_ADMIN_USER_LAST_NAME} --camunda.bpm.admin-user.email=${CAMUNDA_BPM_ADMIN_USER_EMAIL} \
  --event.queue.name=${EVENT_QUEUE_NAME} --tenant.default-tenant=${TENANT_DEFAULT_TENANT} --tenant.initialize-default-tenant=${TENANT_INITIALIZE_DEFAULT_TENANT} \
  --okapi.location=${OKAPI_LOCATION} --spring.h2.console.settings.web-allow-others=${SPRING_H2_WEBALLOW} \
  --okapi.username=${OKAPI_USERNAME} --okapi.password=${OKAPI_PASSWORD}
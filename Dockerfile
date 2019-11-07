#Prerequisites JDK
FROM maven:3.6.1-jdk-8-alpine

#Settings
ENV ARTIFACT_VERSION='1.3.0-SNAPSHOT'
ENV MODULE_VERSION='sprint4-staging'
ENV LOGGING_LEVEL_FOLIO='INFO'
ENV SERVER_PORT='8081'
ENV SPRING_ACTIVEMQ_BROKER_URL='http://mod-workflow:61616'
ENV SPRING_DATASOURCE_PLATFORM='h2'
ENV SPRING_DATASOURCE_URL='jdbc:h2:./camunda-db/camunda;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
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
ENV OKAPI_LOCATION='http://okapi:9130'
ENV OKAPI_USERNAME='tern_admin'
ENV OKAPI_PASSWORD='admin'

#expose port
EXPOSE ${SERVER_PORT}

#Mvn
RUN apk add --no-cache curl git

#install mod-workflow and clone and MVN build mod-data-extractor
RUN mkdir -p /usr/local/bin/folio/
WORKDIR /usr/local/bin/folio
RUN git clone -b ${MODULE_VERSION} https://github.com/TAMULib/mod-workflow.git
WORKDIR /usr/local/bin/folio/mod-workflow
RUN mvn install
WORKDIR /usr/local/bin/folio
RUN git clone -b ${MODULE_VERSION} https://github.com/TAMULib/mod-camunda.git
WORKDIR /usr/local/bin/folio/mod-camunda
RUN mkdir -p camunda-db
RUN mvn package -DskipTests

#run java command
CMD java -jar -Xmx4096m /usr/local/bin/folio/mod-camunda/target/mod-camunda-${ARTIFACT_VERSION}.jar \
    --logging.level.org.folio=${LOGGING_LEVEL_FOLIO} --server.port=${SERVER_PORT} --spring.activemq.broker-url=${SPRING_ACTIVEMQ_BROKER_URL} \
    --spring.datasource.platform=${SPRING_DATASOURCE_PLATFORM} --spring.datasource.url=${SPRING_DATASOURCE_URL} \
    --spring.datasource.driverClassName=${SPRING_DATASOURCE_DRIVERCLASSNAME} --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} --spring.h2.console.enabled=${SPRING_H2_CONSOLE_ENABLED} \
    --spring.jpa.database-platform=${SPRING_JPA_DATABASE_PLATFORM} --camunda.bpm.admin-user.id=${CAMUNDA_BPM_ADMIN_USER_ID} \
    --camunda.bpm.admin-user.password=${CAMUNDA_BPM_ADMIN_USER_PASSWORD} --camunda.bpm.admin-user.first-name=${CAMUNDA_BPM_ADMIN_USER_FIRST_NAME} \
    --camunda.bpm.admin-user.last-name=${CAMUNDA_BPM_ADMIN_USER_LAST_NAME} --camunda.bpm.admin-user.email=${CAMUNDA_BPM_ADMIN_USER_EMAIL} \
    --event.queue.name=${EVENT_QUEUE_NAME} --tenant.default-tenant=${TENANT_DEFAULT_TENANT} --okapi.location=${OKAPI_LOCATION} --spring.h2.console.settings.web-allow-others=${SPRING_H2_WEBALLOW} \
    --okapi.username=${OKAPI_USERNAME} --okapi.password=${OKAPI_PASSWORD}
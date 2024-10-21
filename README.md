# mod-camunda

Copyright (C) 2018-2022 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

# Table of Contents
1. [Docker deployment](#docker-deployment)
    1. [Publish docker image](#publish-docker-image)
2. [Camunda Module Dependencies](#camunda-module-dependencies)
3. [Workflow Project Structure](#workflow-project-structure)
4. [App Deployment](#deploy-and-run-the-application)
5. [Camunda APIs](#camunda-apis)
6. [ActiveMQ Message Broker](#activemq-message-broker)
7. [FOLIO Integration](#folio-integration)
8. [Additional Information](#additional-information)
9. [Issue Tracker](#issue-tracker)

## Docker deployment

```
cd ..
git clone https://github.com/TAMULib/mod-workflow.git
cd mod-workflow
mvn clean install

cd mod-camunda
docker build -t folio/mod-camunda .
docker run -d -p 9000:8081 folio/mod-camunda
```

### Publish docker image

```
docker login [docker repo]
docker build -t [docker repo]/folio/mod-camunda:[version] .
docker push [docker repo]/folio/mod-camunda:[version]
```

## Camunda Module Dependencies
This module extends spring-module-core and brings in Camunda BPM to enable workflow capabilities. Camunda is an open-source BPM platform that is embedded in this module via the following dependencies.
```
# --- VERSIONS ---
<camunda.version>7.9.0</camunda.version>
<camunda.spring.boot.version>3.0.0</camunda.spring.boot.version>

# --- DEPENDENCY MANAGEMENT ---
<dependency>
  <!-- Import dependency management from Camunda -->
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-bom</artifactId>
  <version>${camunda.version}</version>
  <scope>import</scope>
  <type>pom</type>
</dependency>

# --- DEPENDENCIES ---
<dependency>
  <groupId>org.camunda.bpm.springboot</groupId>
  <artifactId>camunda-bpm-spring-boot-starter</artifactId>
  <version>${camunda.spring.boot.version}</version>
</dependency>

<dependency>
  <groupId>org.camunda.bpm.springboot</groupId>
  <artifactId>camunda-bpm-spring-boot-starter-webapp</artifactId>
  <version>${camunda.spring.boot.version}</version>
</dependency>

<dependency>
  <groupId>org.camunda.bpm.springboot</groupId>
  <artifactId>camunda-bpm-spring-boot-starter-rest</artifactId>
  <version>${camunda.spring.boot.version}</version>
</dependency>
```
* camunda-bpm-spring-boot-starter
    * Adds the Camunda engine (v7.9)
    * [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/)
    * [https://github.com/camunda/camunda-bpm-spring-boot-starter](https://github.com/camunda/camunda-bpm-spring-boot-starter)
    * The Camunda engine requires a database schema to be configured on startup
        * Work is in progress to allow the module to start without any database creation and have the tenant creation perform the necessary table creation and initial data import
        * Details on the process engine database schema configuration can be foune [here](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/configuration/)
* camunda-bpm-spring-boot-starter-webapp
    * Enables Web Applications such as Camunda Cockpit and Tasklist
    * [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/webapps/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/webapps/)
* camunda-bpm-spring-boot-starter-rest
    * Enables the Camunda REST API
    * [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/rest-api/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/rest-api/)
    * [https://docs.camunda.org/manual/7.9/reference/rest/](https://docs.camunda.org/manual/7.9/reference/rest/)
    * The Camunda REST API uses Jersey so we use spring boot's common application properties to configure the path to be /camunda in the application.yml file
        * `spring.jersey.application-path=camunda`

## Workflow Project Structure
Business Process Models and Decision Models are built using the [Camunda Modeler](https://camunda.com/products/modeler/) which impelements BPMN 2.0 and DMN 1.1 specifications.

* .bpmn files are stored in `/src/main/java/resources/workflows`
* .dmn files are stored in `/src/main/java/resources/decisions`

Any Java code that is executed in the context of a process is usually written in a Java Delegate. These classes are stored in `/src/main/java/org/folio/rest/delegate/`

## Deploy and run the application
1. Run the application `mvn clean spring-boot:run`
    1. Note there is a hard dependency on ActiveMQ. If running without ActiveMQ, be sure to comment out `activemq.broker-url: tcp://localhost:61616` in the application.yml
2. Deploy all the processes by running scripts/deploy.sh file
3. Navigate to Camunda Portal `localhost:9000/app/welcome/default/#/welcome`
4. Log in as admin username: `admin`, password: `admin`

## Camunda APIs
* Process/Decision Deployment
    * [https://docs.camunda.org/manual/7.9/reference/rest/deployment/](https://docs.camunda.org/manual/7.9/reference/rest/deployment/)
    * GET
        * /camunda/deployment
        * /camunda/deployment/{id}
    * POST
        * /camunda/deployment/create
    * DELETE
        * /camunda/deployment/{id}
* Process Definition
    * [https://docs.camunda.org/manual/7.9/reference/rest/process-definition/](https://docs.camunda.org/manual/7.9/reference/rest/process-definition/)
    * GET
        * /camunda/process-definition
        * /camunda/process-definition/{id}
    * POST
        * /camunda/process-definition/{id}/start
        * /camunda/process-definition/key/{key}/tenant-id/{tenant-id}/start
* Decision Definition
    * [https://docs.camunda.org/manual/7.9/reference/rest/decision-definition/](https://docs.camunda.org/manual/7.9/reference/rest/decision-definition/)
    * GET
        * /camunda/decision-definition
        * /camunda/decision-definition/{id}
* Tasks
    * [https://docs.camunda.org/manual/7.9/reference/rest/task/](https://docs.camunda.org/manual/7.9/reference/rest/task/)
    * GET
        * /camunda/task
        * /camunda/task/{id}
    * POST
        * /camunda/task/{id}/claim
        * /camunda/task/{id}/unclaim
        * /camunda/task/{id}/complete
* Message Events
    * [https://docs.camunda.org/manual/7.9/reference/rest/message/](https://docs.camunda.org/manual/7.9/reference/rest/message/)
    * POST
        * /camunda/message

## ActiveMQ Message Broker
We are using ActiveMQ to consume messages. Currently we are only consuming, not producing messages. This is a hard dependency when running the application, so if you want to run the application without a message broker, comment out `activemq.broker-url: tcp://localhost:61616` in the application.yml

## FOLIO Integration
For detailed information to bring up a FOLIO instance refer to [https://github.com/folio-org/folio-ansible](https://github.com/folio-org/folio-ansible).

The following requires [Vagrant](https://www.vagrantup.com/) 1.9.6 or above.

```
vagrant up
# wait
```

When finished Okapi will be running with mod-workflow and mod-camunda deployed under the diku tenant. mod-camunda will have its port forwarded for access to the Camunda webapps. FOLIO UI will be accessible at `http://localhost:3000`; username: `diku_admin`, password: `admin`.

> Okapi is being built and redeployed from within this vagrant. Eventually this will not need to happen. If a specific branch of either mod-camunda or mod-workflow is desired to be deployed, modify the Vagrantfile `git checkout main` to the desired branch and restart vagrant. `vagrant destroy`, `vagrant up`

### Development

In order to facilitate development on mod-camunda in the context of Okapi, there is a sync directory from the host machine to the guest machine. The host directory is at `.vagrant/sync` and it will contain `okapi`, `mod-camunda`, and `mod-workflow`. The development and git branch management can be done on the host machine. The guest directory is at `/sync`. The redeployment of a module must be done from the guest machine.

```
vagrant ssh
sudo su
cd /sync
# kill mod-camunda running on port 9000
kill $(lsof -t -i :9000)
cd mod-camunda
mvn clean install
nohup java -jar target/mod-camunda-1.1.0.jar &
```

### Login

```
curl -v -H "Content-Type: application/json" -H "X-Okapi-Tenant: diku" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}'
```

The response headers of interest are `X-Okapi-Token` and `refreshToken`.

### Refresh Token

```
curl -v -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/refresh -d '{"refreshToken": "`[refresh token goes here]`"}'
```

The body of this response will contain the new `X-Okapi-Token`.

### Workflow Module Triggers

The Trigger entity from mod-workflow is used to select which request-response events from Okapi are to be published to the `${ENV:folio}.workflow.events` topic that mod-camunda can subscribe to. In order to create the Triggers we have to provide the correct permissions to the `diku_admin`. The vagrant will create an example trigger for when a user is created.

### Permissions

In order to call mod-camunda and mod-workflow through the Okapi gateway a user will need the appropriate permissions. In order to accommodate this the Vagrantfile runs a shell script in which updates permissions for `diku_admin`. Providing him with all permissions to all interfaces of mod-camunda and mod-workflow.

### Cleanup

When finished it will be desired to cleanup as the FOLIO vagrant uses a lot of resources.

```
vagrant destroy
rm -rf .vagrant/sync
```

> Removing the synced directory, `.vagrant/sync`, on the host will remove any changes during development that have not been pushed.

### Environment variables:

| Name                              |       Default value       | Description                                                                                                                                                |
|:----------------------------------|:-------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| JAVA_OPTIONS                      | -XX:MaxRAMPercentage=75.0 | Java options                                                                                                                                               |
| CAMUNDA_BPM_ADMINUSER_EMAIL       |         admin@localhost          | The e-mail address of the Camunda administration user.                                                                                                                                          |
| CAMUNDA_BPM_ADMINUSER_ID          |           admin            | The account name of the Camunda administration user.                                                                                                                                              |
| CAMUNDA_BPM_ADMINUSER_PASSWORD    |        admin        | The password of the Camunda administration user.                                                                                                                                          |
| CAMUNDA_BPM_DATABASE_SCHEMAUPDATE |            true             | If Camunda should auto-update the BPM database schema.                                                                                                                                 |
| CAMUNDA_BPM_METRICS               |             false             | Enable or disable Camunda metrics by default.                                                                                                                                 |
| DB_HOST                           |         postgres          | Postgres hostname                                                                                                                                          |
| DB_PORT                           |           5432            | Postgres port                                                                                                                                              |
| DB_USERNAME                       |        folio_admin        | Postgres username                                                                                                                                          |
| DB_PASSWORD                       |             -             | Postgres username password                                                                                                                                 |
| DB_DATABASE                       |       okapi_modules       | Postgres database name                                                                                                                                     |
| DB_QUERYTIMEOUT                   |           60000           | Database query timeout.                                                                                                                                    |
| DB_CHARSET                        |           UTF-8           | Database charset.                                                                                                                                          |
| DB_MAXPOOLSIZE                    |             5             | Database max pool size.                                                                                                                                    |
| KAFKA_HOST                        |           kafka           | Kafka broker hostname                                                                                                                                      |
| KAFKA_PORT                        |           9092            | Kafka broker port                                                                                                                                          |
| KAFKA_SECURITY_PROTOCOL           |         PLAINTEXT         | Kafka security protocol used to communicate with brokers (SSL or PLAINTEXT)                                                                                |
| KAFKA_SSL_KEYSTORE_LOCATION       |             -             | The location of the Kafka key store file. This is optional for client and can be used for two-way authentication for client.                               |
| KAFKA_SSL_KEYSTORE_PASSWORD       |             -             | The store password for the Kafka key store file. This is optional for client and only needed if 'ssl.keystore.location' is configured.                     |
| KAFKA_SSL_TRUSTSTORE_LOCATION     |             -             | The location of the Kafka trust store file.                                                                                                                |
| KAFKA_SSL_TRUSTSTORE_PASSWORD     |             -             | The password for the Kafka trust store file. If a password is not set, trust store file configured will still be used, but integrity checking is disabled. |
| OKAPI_URL                         |     http://okapi:9130     | OKAPI URL used to login system user, required                                                                                                              |
| SERVER_PORT                       |           8081            | The port to listen on that must match the PortBindings.                                                                                                              |
| SERVER_SERVLET_CONTEXTPATH        |             /             | The context path, or base path, to host at.                                                                                                              |
| SPRING_FLYWAY_ENABLED             |           false           | Database migration support via Spring Flyway.                                                                                                              |
| SPRING_JPA_HIBERNATE_DDLAUTO      |           update          | Auto-configure database on startup.                                                                                                              |
| TENANT_DEFAULTTENANT              |           diku            | The name of the default tenant to use.                                                                                                              |
| TENANT_FORCETENANT                |           false           | Forcibly add or overwrite the tenant name using the default tenant.                                                                                                              |
| TENANT_INITIALIZEDEFAULTTENANT    |           true            | Perform initial auto-creation of tenant in the DB (schema, tables, etc..).                                                                                                              |
| TENANT_RECREATEDEFAULTTENANT      |           false           | When TENANT_INITIALIZEDEFAULTTENANT is true and the DB already exists, then drop and re-create.                                                                                                              |

### Required Permissions
Institutional users should be granted the following permissions in order to use this remote storage API:
- `camunda.history.all`
- `camunda.message.all`
- `camunda.process.all`
- `camunda.process-definition.all`
- `camunda.decision-definition.all`
- `camunda.task.all`
- `camunda.workflow-engine.workflows.all`

## Additional information

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

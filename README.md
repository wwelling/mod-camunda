# mod-camunda

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

# Table of Contents
1. [Camunda Module Dependencies](#camunda-module-dependencies)
2. [Workflow Project Structure](#workflow-project-structure)
3. [App Deployment](#deploy-and-run-the-application)
4. Test Processes
    1. [Test Master Processes](#test-master-process)
    2. [Test Process 1](#test-process-1)
    3. [Test Process 2](#test-process-2)
    4. [Test Process 3](#test-process-3)
5. FOLIO POC Processes
    1. [Claim Returned](#claim-return)
    2. [Purchase Request](#purchase-request)
    3. [Folio Login Sample](#folio-login-sample)
6. [Camunda APIs](#camunda-apis)
7. [ActiveMQ Message Broker](#activemq-message-broker)
8. [FOLIO Integration](#folio-integration)
9. [Additional Information](#additional-information)
10. [Issue Tracker](#issue-tracker)

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
    1. Note there is a hard dependency on ActiveMQ, if running without ActiveMQ, be sure to comment out `activemq.broker-url: tcp://localhost:61616` in the application.yml
2. Deploy all the processes by running scripts/deploy.sh file
3. Navigate to Camunda Portal `localhost:9000/app/welcome/default/#/welcome`
4. Log in as admin username: `admin`, password: `admin`

## Test Master Process
This is the master process which can start the other processes as well as send events.
1. Navigate to the Camunda Tasklist
2. Start "Test Master Process"
3. Upon starting, you will receive a "Choose Path" task
4. Claim the "Choose Path" task and select an option from the dropdown
    1. Selecting "Send Message" will start "Test Process 2" by sending a Start Message Event
        1. After starting the process, you will receive a "Input Correlation" task. The message correlation is the (randomly generated) unique business key that targets the process instance you are sending a message to. This pre-populates, but is editable.
        2. Claim and complete this task to send an Intermediate Message Event to the "Test Process 2" instance that was just started
        3. If the message correlates properly, it will advance the token and you will receive a "User Task" task
        4. If you do not complete the "User Task" within 5 minutes, a timer will interrupt the task and complete the instance
    2. Selecting "Send Task" will start "Test Process 3"
        1. After starting the process, you will receive a "Input Correlation" task. The message correlation is the (randomly generated) unique business key that targets the process instance you are sending a message to. This pre-populates, but is editable.
        2. Claim and complete this task to send a Send Task Event to the "Test Process 3" instance that was just started
        3. If the message correlates properly, it will advance the token and will wait on an External Task step
    3. Selecting "Manual Task" will go to a pass through manual task
    4. Selecting "Service Task" will run a delegate (not yet implemented)
    5. Selecting "Script Task" will run a script (not yet implemented)
    6. Selecting "Call Activity" will will start "Test Process 1"
5. If you selected options 3-6, you will have the option to "Retry" the process. This just loops back to the first dropdown and allows you to select a new event.

## Test Process 1
Steps to run TestProcess1

1. Run the application `mvn clean spring-boot:run`
2. Register 'TestProcess1' using REST API
    1. ```curl -X POST -H "X-Okapi-Tenant: diku" -F "tenant-id=diku" -F "deployment-name=TestProcess1" -F "deployment-source=process application" -F "data=@src/main/resources/workflows/TestProcess1.bpmn" http://localhost:9000/camunda/deployment/create```
3. Navigate to Camunda Portal `localhost:9000/app/welcome/default/#/welcome`
4. Log in as admin username: `admin`, password: `admin`
5. Select Tasklist from the Dashboard
6. In top right corner click "Start process", select "TestProcess1", click "start"
7. Refresh Tasklist page or click "All Tasks" and you should see a new task generated
8. Select "Task1" and click "Claim" in top right corner of task form
    1. Task should have two variable fields pre-populated. You can change these if you would like
    2. Task has boolean option "Throw Error?" to check if you want to generate the error handling task
9.  "Complete" the task

### Notes about "Test Process 1"
* Manual Start Event
  * Has one start form variable `startVariable` that defaults to "hello"

* System1 Task is a Delegate expression `${system1Delegate}` which is found at `org.folio.rest.delegate.System1Delegate`.
  * This delegate has some logging of various process attributes
  * This delegate also adds `delegateVariable` "SampleStringVariable" to the process context

* Task1 pre-populates two fields with the already initialized process variables
  * This task also has a new boolean variable `throwError`

* Throw Runtime Error task is a Java Class implementation (just another way of doing things other than Delegate Expression) which is found at `org.folio.rest.delegate.ThrowRuntimeErrorDelegate`
  * This task intentionally throws a runtime IndexOutOfBoundsException
  * We catch this exception and start an error handling sub-process

## Test Process 2
Process can be started via the following methods
* Manually via Camunda Tasklist
* Message Start Event sent from "Test Process Master"
* REST API, POST to `localhost:9000/camunda/message`

If starting from the REST API, below is a sample payload. Documentation found [here](https://docs.camunda.org/manual/7.9/reference/rest/message/post-message/).
```
{
  "messageName" : "Message_StartProcess2",
  "businessKey" : "aBusinessKey1",
  "tenantId" : "diku",
  "processVariables" : {
    "startMessageVariable" : {"value" : "aNewValue2", "type": "String" }
  }
}
```
After starting, this process has the following activities
* System Delegate - same delegate as "Test Process 1"
* Decision Table - a sample decision table
    * Deployed "Decision1.dmn"
    * Can visualize this table in Camunda Modeler (same for .bpmn process diagrams)
    * Decision takes in a variable created in the first delegate and maps a simple String output
    * Decision table is tenant aware via `${execution.tenantId}`
* (Catch) Intermediate Message Event
    * Message Name: "Message_ReceiveEvent1"
    * Token waits here until a message is received that correlates to the active instance
    * Message is sent from "Test Master Process" but can also be sent from REST API `localhost:9000/camunda/message` with below payload
```
{
  "messageName" : "Message_ReceiveEvent1",
  "businessKey" : "pk11224",
  "tenantId" : "diku"
}
```
* User Task with a Timer Event
    * Timer event is set to trigger (and interrupt the task) after 5 minutes to complete the instance

## Test Process 3
After starting, this process has the following activities
* Receive Task
    * Message Name: "Message_ReceiveTask1"
    * Similar to Catch Intermediate Message Event
    * Message is sent from "Test Master Process" but can also be sent from REST API `localhost:9000/camunda/message` with below payload
```
{
  "messageName" : "Message_ReceiveTask1",
  "businessKey" : "pk19668",
  "tenantId" : "diku"
}
```
* External Task
    * Topic: ExternalTask1

## Claim Return
The Claim Return Process was identified as a candidate for the workflow POC. Some of the functionality required for this process is not yet in FOLIO, so the triggers are in different places for the purpose of the POC.

#### Business Case Requirements for Claim Returned
* A process is started when a claim is marked as "claim returned" from a students profile
    * This should send an event to start a Camunda process with a data payload
* There will be a separate dashboard displayed with all of the open claims (out of scope for this project)
    * A user can select any number of claims from this list and manually check for them
    * After manually checking for them, a user could send an event or update based on their findings
    * There is a timer that can be configured to notify the user a certain amount of time has passed since the item was last searched for
    * A number of actions can be taken based on the output of the users findings
        * Checked in
        * Lost item
        * Missing item
        * Increment the count (we can configure the max number of counts as well)
    * NOTE: The current implementation has the following integration
* The process can be interrupted at any point if the book is checked in from an external source

#### Current POC Implementation
* The current implementation starts the process when a book is checked out to a patron since the claims returned functionality is not yet developed in FOLIO.
    * There is a trigger that is set up to send an event to the message queue that `mod-camunda` is listening to when a POST request is made to `/circulation/check-out-by-barcode`
    * When `mod-camunda` receives this event, it will start the Claim Returned process with the payload
* A decision has yet to be made on whether or not this process will do "monitoring" or "managing", so for the sake of the POC, it can do a bit of both, but only one at a time
    * For "monitoring", after the process is started, we want to be able to advance the process if the relevant item is checked in from an external source within FOLIO
        * To handle this, we have another trigger listening to PUT requests at `/circulation/loans/{loanId}`
        * When an update (check-in) happens, we will correlate a message to the existing process instance and have a delegate log that an external check-in has ocurred
        * In the future, this delegate could be updated to send an email or notification
    * For "managing" the process, once it is started, a user task is created to search for the item and make a decision
        * Check in - the item was found and a delegate will make an Okapi request to check the book in (without having to touch the FOLIO UI)
        * Lost item - declare the item lost, a delegate will first update the status of the loan by making an Okapi request, then a separate delegate will make another Okapi request to create a notification for the patron
        * Missing item - declare the item missing, currently this functionality is not in FOLIO, for the sake of the POC, a delegate will make an Okapi request to renew the item
        * Increment count - simply increment the count until a max of 3, after 3 times, the user will need to make a decision to declare the item lost, missing, or end the process
        * NOTE: to perform any of these actions that "manage" the process, we need to remove the trigger at `circulation/loans/{loanId}` or disable the `CORRELATE_MESSAGE` since it will cause conflicts

## Purchase Request
The Purchase Request Process was identified as a candidate for the workflow POC
* A process can be started with a "start form" directly from the Camunda Tasklist, or can be started from the REST API with a JSON payload
* After a process is started there is a "Selector" task
* If the Selector chooses to fund the request, a "Fund" task will be selected and the output of this task is a new `orderId`
* After the "Fund" task, an event is thrown that notifies a new `orderId` has been created and the process waits for a message event that the order has been received
* Once the order has been received via a message event, it will send a notification message

## Folio Login Sample
Sample process to prototype the following
* Logging into Folio and getting an `x-okapi-token` and `refreshtoken` to store in the process context for future Okapi calls
* Prototype how to use the `refreshtoken` to get a new `x-okapi-token`
* After logging in, using the token to make an Okapi request, parse the response, and save to the process context

To run the process, we need to have a Folio instance running with `mod-camunda` and have the "Folio Login Sample" process deployed
1. Start the process from the TaskList and be sure to add a `businessKey`, in this case we will use "A001"
    1. Once the process is started, we make a call to Okapi to log in to Folio, then we have a wait event
2. Trigger the wait event by sending the following message correlation payload to `localhost:9000/camunda/message`
   1. Note the `businessKey` must match the one we started the process with
   2. If you want to clear the original token, and test the refreshtoken, change the `retryLogin` value to "yes"
   3. Note that in this case we are adding a new user to Folio by making a POST request to `/users`
   4. Note that the `requestPayload` must be formatted as an escaped JSON String
```
{
  "messageName" : "MessageLoginWaitEvent",
  "businessKey" : "A001",
  "tenantId" : "diku",
  "processVariables" : {
    "okapiRequest" : {"value" : "{\r\n    \t\"requestUrl\" : \"http://localhost:9130/users\",\r\n    \t\"requestMethod\" : \"POST\",\r\n
        \t\"requestContentType\" : \"application\/json\",\r\n    \t\"requestPayload\" : {\r\n    \t\t\"id\" : \"e6ea799c-bc30-11e8-a355-529269fb1459\",\r\n
        \t\t\"username\" : \"eexciting \"\r\n    \t},\r\n    \t\"responseStatusName\" : \"status\",\r\n    \t\"responseHeaderName\" : \"name\",\r\n
        \t\"responseBodyName\" : \"body\"\r\n    }",
    "type": "String"
    },
    "retryLogin" : {"value" : "no", "type" : "String"}
  }
}
```
After triggering the message, the Okapi request will be made, the token will advance, and we should be able to inspect the response via the Camunda Cockpit


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

When finished Okapi will be running with mod-workflow and mod-camunda deployed under the diku tenant. mod-camunda will have its port forwarded for access to the Camunda webapps. FOLIO UI will be accessable at `http://localhost:3000`; username: `diku_admin`, password: `admin`.

> Okapi is being built and redeployed from within this vagrant. Eventually this will not need to happen. If a specific branch of either mod-camunda or mod-workflow is desired to be deployed, modify the Vagrantfile `git checkout master` to the desired branch and restart vagrant. `vagrant destroy`, `vagrant up`

### Development

In order to facilitate development on mod-camunda in the context of Okapi there is a sync directory from the host machine to the guest machine. The host directory is at `.vagrant/sync` and it will contain `okapi`, `mod-camunda`, and `mod-workflow`. The development and git branch management can be done on the host machine. The guest directory is at `/sync`. The redeployment of a module must be done from the guest machine.

```
vagrant ssh
sudo su
cd /sync
# kill mod-camunda running on port 9000
kill $(lsof -t -i :9000)
cd mod-camunda
mvn clean install
nohup java -jar target/mod-camunda-1.0.0-SNAPSHOT.jar &
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

The Trigger entity from mod-workflow is used to select which request-response events from Okapi are to be published to the `event.queue` that mod-camunda can subscribe to. In order to create the Triggers we have to provide the correct permissions to the `diku_admin`. The vagrant will create an example trigger for when a user is created.

### Permissions

In order to call mod-camunda and mod-workflow through the Okapi gateway a user will need the appropriate permissions. In order to accomdate this the Vagrantfile runs a shell script in which updates permissions for `diku_admin`. Providing him with all permissions to all interfaces of mod-camunda and mod-workflow.

### Cleanup

When finished it will be desired to cleanup as the FOLIO vagrant uses a lot of resources.

```
vagrant destroy
rm -rf .vagrant/sync
```

> Removing the synced directory, `.vagrant/sync`, on the host will remove any changes during development that have not been pushed.

## Additional information

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

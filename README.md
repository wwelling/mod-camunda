# mod-camunda

Copyright (C) 2016-2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Deploy and run the application
1. Run the application `mvn clean spring-boot:run`
2. Deploy all the processes by running scripts/deploy.sh file
3. Navigate to Camunda Portal `localhost:9000/app/welcome/default/#/welcome`
4. Log in as admin username: `admin`, password: `admin`

## Camunda "Test Master Process"
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

## Camunda "Test Process 1"
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

## Camunda "Test Process 2"
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
    * Token waits here until a message is received that correlates to the active instance
* User Task with a Timer Event
    * Timer event is set to trigger (and interrupt the task) after 5 minutes to complete the instance

## Camunda "Test Process 3"
After starting, this process has the following activities
* Receive Task
    * Similar to Catch Intermediate Message Event
* External Task
    * Topic: ExternalTask1

## Additional information

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

# mod-camunda

Copyright (C) 2016-2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.

## Camunda TestProcess1
Steps to run TestProcess1

1. Run the application `mvn clean spring-boot:run`
1. Navigate to Camunda Portal `localhost:9000/app/welcome/default/#/welcome`
1. Log in as admin username: `admin`, password: `admin`
1. Select Tasklist from the Dashboard
1. In top right corner click "Start process", select "TestProcess1", click "start"
1. Refresh Tasklist page or click "All Tasks" and you should see a new task generated
1. Select "Task1" and click "Claim" in top right corner of task form
    1. Task should have two variable fields pre-populated. You can change these if you would like
    1. Task has boolean option "Throw Error?" to check if you want to generate the error handling task
1. "Complete" the task

### Notes about TestProcess1
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

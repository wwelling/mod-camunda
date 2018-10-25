# Camunda Postman Collection
This Postman collection contains API calls with sample payloads for both general Camunda calls and FOLIO process specific calls. It can be imported directly into Postman.

## Camunda
Details about Camunda specific calls
* **Get Tasks** 
    * Get list of all open tasks. Can also pass in filters (no filters currently used in Postman collection)
    * [https://docs.camunda.org/manual/7.9/reference/rest/task/get-query/](https://docs.camunda.org/manual/7.9/reference/rest/task/get-query/)
* **Get Tasks Filtered**
    * Get filtered list of tasks, slightly more powerful than above query, allows for filtering by multiple process or task variables
    * Current Postman sample filters by process, task name, and tenantId
    * [https://docs.camunda.org/manual/7.9/reference/rest/task/post-query/](https://docs.camunda.org/manual/7.9/reference/rest/task/post-query/) 
* **Complete Task by Id**
    * Completes task and updates process variables
    * Current Postman sample has variables required to make an Okapi request/response
    * [https://docs.camunda.org/manual/7.9/reference/rest/task/post-complete/](https://docs.camunda.org/manual/7.9/reference/rest/task/post-complete/)


## Claim Returned Process
* **Start**
    * This message starts the Claim Returned Process with a set of variables
        * `businessKey` - a unique identifier for the process instance
        * `tenantId`
        * `processVariables` - currently dummy data, will update with real world data when we have integrations to FOLIO and Okapi
* **Update Claim Message**
    * This message can be sent to update the claim with a `claimAction`
    * Note this can be completed as a task from the tasklist as well. Sending this payload will interrupt the task and cancel it. 
* **External Update Message**
    * This message interrupts the process that a claim has been updated from an external source and terminates the process
    * The message correlates to the `messageName` (specific to this process), the `businessKey` (unique identifier of the process instance), and the `tenantId`

## Purchase Request Process
* **Start**
    * This message starts the Purchase Request Process with a set of variables
       * `tenantId`
       * `processVariables` - currently dummy data, will update with real world data when we have integrations to FOLIO and Okapi
* **Receive Message**
    * This message is meant to simulate an order being received
    * Once an order for the request has been created, the process waits for this message to be sent in order to advance the token
    * The message is correlated by the `bookId` and `orderId` 

package org.folio.rest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.TerminateEventDefinition;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaString;
import org.folio.rest.model.AccumulatorTask;
import org.folio.rest.model.StreamCreateForEachTask;
import org.folio.rest.model.EventTrigger;
import org.folio.rest.model.LoginTask;
import org.folio.rest.model.ProcessorTask;
import org.folio.rest.model.StreamingExtractorTask;
import org.folio.rest.model.Task;
import org.folio.rest.model.Trigger;
import org.folio.rest.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BpmnModelFactory {

  private static final String END_EVENT_ID = "ee_0";
  private static final String START_EVENT_ID = "se_0";
  private static final String TARGET_NAMESPACE = "http://bpmn.io/schema/bpmn";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public BpmnModelInstance makeBPMNFromWorkflow(Workflow workflow) {

    BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

    // Setup Definitions
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace(TARGET_NAMESPACE);
    modelInstance.setDefinitions(definitions);

    // Setup Process
    Process process = createElement(modelInstance, definitions, workflow.getName().replaceAll(" ", "_").toLowerCase(), Process.class);
    process.setExecutable(true);
    process.setName(workflow.getName());

    // Setup Nodes

    StartEvent processStartEvent = createElement(modelInstance, process, START_EVENT_ID, StartEvent.class);
    processStartEvent.setName("StartProcess");

    List<ServiceTask> serviceTasks = new ArrayList<ServiceTask>();
    AtomicInteger taskIndex = new AtomicInteger();

    int loginIndex = taskIndex.getAndIncrement();
    ServiceTask loginServiceTask = createElement(modelInstance, process, String.format("t_%s", loginIndex), ServiceTask.class);
    LoginTask loginTask = new LoginTask("LoginProcess");
    serviceTasks.add(enhanceServiceTask(loginServiceTask, loginTask));

    if (workflow.getTasks().stream().anyMatch(t -> t.isStreaming())) {
      int index = taskIndex.getAndIncrement();
      ServiceTask createPrimaryStream = createElement(modelInstance, process, String.format("t_%s", index), ServiceTask.class);
      createPrimaryStream.setName("Create Primary Stream");
      createPrimaryStream.setCamundaDelegateExpression("${streamCreationDelegate}");
      serviceTasks.add(createPrimaryStream);
    }

    serviceTasks.addAll(workflow.getTasks().stream().map(task -> {
      int index = taskIndex.getAndIncrement();
      ServiceTask serviceTask = createElement(modelInstance, process, String.format("t_%s", index), ServiceTask.class);
      if(task instanceof StreamingExtractorTask) {
        StreamingExtractorTask eTask = (StreamingExtractorTask) task;
        ExtensionElements extensionElements = createElement(modelInstance, serviceTask, null, ExtensionElements.class);
        CamundaField streamSource = createElement(modelInstance, extensionElements, String.format("t_%s-stream-source", index), CamundaField.class);
        streamSource.setCamundaName("streamSource");
        streamSource.setCamundaStringValue(eTask.getStreamSource());
        CamundaField comparisonProperty = createElement(modelInstance, extensionElements, String.format("t_%s-comparison-property", index), CamundaField.class);
        comparisonProperty.setCamundaName("comparisonProperty");
        comparisonProperty.setCamundaStringValue(eTask.getPredicateProperty());
      } else if(task instanceof ProcessorTask) {
        ProcessorTask pTask = (ProcessorTask) task;
        ExtensionElements extensionElements = createElement(modelInstance, serviceTask, null, ExtensionElements.class);
        CamundaField scriptTypeField = createElement(modelInstance, extensionElements, String.format("t_%s-script-type", index), CamundaField.class);
        scriptTypeField.setCamundaName("scriptType");
        scriptTypeField.setCamundaStringValue(pTask.getScriptType().engineName);
        CamundaField scriptField = createElement(modelInstance, extensionElements, String.format("t_%s-script", index), CamundaField.class);
        scriptField.setCamundaName("script");
        CamundaString script = createElement(modelInstance, scriptField, null, CamundaString.class);
        script.setTextContent(pTask.getScript());
      } else if (task instanceof StreamCreateForEachTask) {
        StreamCreateForEachTask cTask = (StreamCreateForEachTask) task;
        ExtensionElements extensionElements = createElement(modelInstance, serviceTask, null, ExtensionElements.class);
        CamundaField endpoint = createElement(modelInstance, extensionElements, String.format("t_%s-endpoint", index), CamundaField.class);
        endpoint.setCamundaName("endpoint");
        endpoint.setCamundaStringValue(cTask.getEndpoint());
        CamundaField target = createElement(modelInstance, extensionElements, String.format("t_%s-target", index), CamundaField.class);
        target.setCamundaName("target");
        target.setCamundaStringValue(cTask.getTarget());
        CamundaField source = createElement(modelInstance, extensionElements, String.format("t_%s-source", index), CamundaField.class);
        source.setCamundaName("source");
        source.setCamundaStringValue(cTask.getSource());
        CamundaField uniqueBy = createElement(modelInstance, extensionElements, String.format("t_%s-uniqueBy", index), CamundaField.class);
        uniqueBy.setCamundaName("uniqueBy");
        uniqueBy.setCamundaStringValue(StringUtils.isEmpty(cTask.getUniqueBy()) ? "NO_VALUE" : cTask.getUniqueBy());
      } else if(task instanceof AccumulatorTask) {
        AccumulatorTask aTask = (AccumulatorTask) task;
        ExtensionElements extensionElements = createElement(modelInstance, serviceTask, null, ExtensionElements.class);
        CamundaField accumulateToField = createElement(modelInstance, extensionElements, String.format("t_%s-accumulate-to", index), CamundaField.class);
        accumulateToField.setCamundaName("accumulateTo");
        accumulateToField.setCamundaStringValue(Long.toString(aTask.getAccumulateTo()));
        CamundaField delayDuration = createElement(modelInstance, extensionElements, String.format("t_%s-delay-duration", index), CamundaField.class);
        delayDuration.setCamundaName("delayDuration");
        delayDuration.setCamundaStringValue(Long.toString(aTask.getDelayDuration()));
        CamundaField storageDestination = createElement(modelInstance, extensionElements, String.format("t_%s-storage-destination", index), CamundaField.class);
        storageDestination.setCamundaName("storageDestination");
        storageDestination.setCamundaStringValue(aTask.getStorageDestination());
      }
      return enhanceServiceTask(serviceTask, task);
    }).collect(Collectors.toList()));

    EndEvent processEndEvent = createElement(modelInstance, process, END_EVENT_ID, EndEvent.class);
    processEndEvent.setName("EndProcess");
    createElement(modelInstance, processEndEvent, null, TerminateEventDefinition.class);

    SequenceFlow firstSf = createElement(modelInstance, process, String.format("%s-%s", START_EVENT_ID, "t_1"), SequenceFlow.class);
    firstSf.setSource(processStartEvent);
    firstSf.setTarget(serviceTasks.get(0));

    // Setup Connections
    AtomicInteger sfIndex = new AtomicInteger();
    serviceTasks.forEach(st->{
      int currentIndex = sfIndex.getAndIncrement();
      if (currentIndex != serviceTasks.size() - 1) {
        SequenceFlow currentSf = createElement(modelInstance, process, String.format("t_%s-t_%s", currentIndex, currentIndex+1), SequenceFlow.class);
        currentSf.setTarget(serviceTasks.get(currentIndex + 1));
        currentSf.setSource(st);
      }
    });

    SequenceFlow lastSf = createElement(modelInstance, process, String.format("t_%s-%s", serviceTasks.size(), END_EVENT_ID), SequenceFlow.class);
    lastSf.setSource(serviceTasks.get(serviceTasks.size() - 1));
    lastSf.setTarget(processEndEvent);

    Message processStartMessage = createElement(modelInstance, definitions, "process-start-message", Message.class);
    Trigger trigger = workflow.getStartTrigger();
    if (trigger instanceof EventTrigger) {
      EventTrigger eventTrigger = (EventTrigger) trigger;
      processStartMessage.setName(eventTrigger.getPathPattern());
    }

    MessageEventDefinition processStartEventMessageDefinition = createElement(modelInstance, processStartEvent, "process-start-event-message-definition", MessageEventDefinition.class);
    processStartEventMessageDefinition.setMessage(processStartMessage);

    // Stub Empty Diagram
    BpmnDiagram diagramElement = createElement(modelInstance, definitions, String.format("%s-diagram", workflow.getName().replaceAll(" ", "_").toLowerCase()), BpmnDiagram.class);

    BpmnPlane plane = createElement(modelInstance, diagramElement, String.format("%s-plane", workflow.getName().replaceAll(" ", "_").toLowerCase()), BpmnPlane.class);
    plane.setBpmnElement(process);

    return modelInstance;
  }

  // @formatter:off
  protected <T extends BpmnModelElementInstance> T createElement(
    BpmnModelInstance modelInstance,
    BpmnModelElementInstance parentElement,
    String id,
    Class<T> elementClass
  ) {
  // @formatter:on
    T element = modelInstance.newInstance(elementClass);
    if(id != null) element.setAttributeValue("id", id, true);
    parentElement.addChildElement(element);
    return element;
  }

  public Message createMessage(BpmnModelInstance modelInstance, Process process, String id, String textContent) {
    Message message = createElement(modelInstance, process, id, Message.class);
    message.setTextContent(textContent);
    return message;
  }

  public BoundaryEvent createBoundaryEvent(BpmnModelInstance modelInstance, Process process, String id, String textContent) {
    BoundaryEvent boundaryEvent = createElement(modelInstance, process, id, BoundaryEvent.class);
    return boundaryEvent;
  }

  public SequenceFlow createSequenceFlow(BpmnModelInstance modelInstance, SubProcess subProcess, FlowNode from, FlowNode to) {
    String id = from.getId() + "-" + to.getId();
    SequenceFlow sequenceFlow = createElement(modelInstance, subProcess, id, SequenceFlow.class);
    subProcess.addChildElement(sequenceFlow);
    sequenceFlow.setSource(from);
    from.getOutgoing().add(sequenceFlow);
    sequenceFlow.setTarget(to);
    to.getIncoming().add(sequenceFlow);
    return sequenceFlow;
  }

  private ServiceTask enhanceServiceTask(ServiceTask serviceTask, Task task) {
    serviceTask.setName(task.getName());
    serviceTask.setCamundaDelegateExpression(String.format("${%s}",task.getDelegate()));
    return serviceTask;
  }

}

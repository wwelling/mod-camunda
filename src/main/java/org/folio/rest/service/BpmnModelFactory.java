package org.folio.rest.service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.delegate.AbstractWorkflowDelegate;
import org.folio.rest.delegate.SetupDelegate;
import org.folio.rest.workflow.model.EventTrigger;
import org.folio.rest.workflow.model.ScheduleTrigger;
import org.folio.rest.workflow.model.Task;
import org.folio.rest.workflow.model.Trigger;
import org.folio.rest.workflow.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BpmnModelFactory {

  private final static Logger logger = LoggerFactory.getLogger(BpmnModelFactory.class);

  private final static String SETUP_TASK_ID = "setup_task";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SetupDelegate setupDelegate;
  
  @Autowired
  private List<AbstractWorkflowDelegate> workflowDelegates;

  public BpmnModelInstance fromWorkflow(Workflow workflow) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess()
      .name(workflow.getName());

    StartEventBuilder startEventBuilder = startEvent(processBuilder, workflow.getStartTrigger());

    ServiceTaskBuilder serviceTaskBuilder = tasks(startEventBuilder, workflow.getTasks());

    BpmnModelInstance model = serviceTaskBuilder.endEvent().done();

    taskExpressions(model, workflow.getTasks());

    initialContextExpression(model, workflow.getInitialContext());

    return model;
  }

  private StartEventBuilder startEvent(ProcessBuilder processBuilder, Trigger trigger) {
    StartEventBuilder startEventBuilder = processBuilder.startEvent()
      .name(trigger.getName());
    if (trigger instanceof EventTrigger) {
      startEventBuilder
        .message(((EventTrigger) trigger).getPathPattern());
    } else if (trigger instanceof ScheduleTrigger) {
      startEventBuilder
        .timerWithCycle(((ScheduleTrigger)trigger).getChronExpression());
    }
    return startEventBuilder;
  }

  private ServiceTaskBuilder tasks(StartEventBuilder startEventBuilder, List<Task> tasks) {
    AtomicInteger index = new AtomicInteger(1);
    ServiceTaskBuilder serviceTaskBuilder = startEventBuilder
      .serviceTask(SETUP_TASK_ID)
      .name("Setup")
      .camundaDelegateExpression(setupDelegate.getExpression());
    for (Task task : tasks) {
      Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
        .filter(d -> d.fromTask().equals(task.getClass()))
        .findAny();
      if (delegate.isPresent()) {
        serviceTaskBuilder = serviceTaskBuilder
          .serviceTask(task.id(index.getAndIncrement()))
          .name(task.getName())
          .camundaDelegateExpression(delegate.get().getExpression());
      } else {
        logger.warn("No delegate from {} found", task.getClass());
      }
    }
    return serviceTaskBuilder;
  }

  private void taskExpressions(BpmnModelInstance model, List<Task> tasks) {
    AtomicInteger index = new AtomicInteger(1);
    tasks.stream()
      .filter(task -> workflowDelegates.stream()
        .anyMatch(d -> d.fromTask().equals(task.getClass()))).forEach(task -> {
      ExtensionElements extensions = model.newInstance(ExtensionElements.class);
      for (Field f : task.getClass().getDeclaredFields()) {
        f.setAccessible(true);

        CamundaField field = model.newInstance(CamundaField.class);
        field.setCamundaName(f.getName());

        try {
          if (String.class.isAssignableFrom(f.getType()) ||
              Number.class.isAssignableFrom(f.getType()) ||
              Boolean.class.isAssignableFrom(f.getType()) ||
              Enum.class.isAssignableFrom(f.getType())) {
            field.setCamundaStringValue(f.get(task).toString());
          } else {
            field.setCamundaStringValue(objectMapper.writeValueAsString(f.get(task)));
          }
        } catch (JsonProcessingException e) {
          logger.warn("Failed to serialize field {} of task {}", f.getName(), task.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
          logger.warn("Unknown field {} of task {}", f.getName(), task.getClass().getSimpleName());
        } catch (IllegalAccessException e) {
          logger.warn("Cannot access field {} of task {}", f.getName(), task.getClass().getSimpleName());
        }

        extensions.addChildElement(field);
      }
      ModelElementInstance element = model.getModelElementById(task.id(index.getAndIncrement()));
      element.addChildElement(extensions);
    });
  }

  private void initialContextExpression(BpmnModelInstance model, Map<String, String> initialContext) {
    ExtensionElements extensions = model.newInstance(ExtensionElements.class);
    CamundaField field = model.newInstance(CamundaField.class);
    field.setCamundaName("initialContext");
    try {
      field.setCamundaStringValue(objectMapper.writeValueAsString(initialContext));
    } catch (JsonProcessingException e) {
      logger.warn("Failed to serialize initial context");
    }
    extensions.addChildElement(field);
    ModelElementInstance element = model.getModelElementById(SETUP_TASK_ID);
    element.addChildElement(extensions);
  }

}

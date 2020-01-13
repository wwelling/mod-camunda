package org.folio.rest.service;

import java.lang.reflect.Field;
import java.util.List;
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
import org.folio.rest.delegate.AbstractRuntimeDelegate;
import org.folio.rest.workflow.model.EventTrigger;
import org.folio.rest.workflow.model.ManualTrigger;
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

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private List<AbstractRuntimeDelegate> delegates;

  public BpmnModelInstance fromWorkflow(Workflow workflow) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess().name(workflow.getName());

    StartEventBuilder startEventBuilder = startEvent(processBuilder, workflow.getStartTrigger());

    ServiceTaskBuilder serviceTaskBuilder = tasks(startEventBuilder, workflow.getTasks());

    // TODO: use super class to avoid null check
    BpmnModelInstance model = serviceTaskBuilder != null ? 
      serviceTaskBuilder.endEvent().done() :
        startEventBuilder.endEvent().done();

    enhance(model, workflow.getTasks());

    return model;
  }

  private StartEventBuilder startEvent(ProcessBuilder processBuilder, Trigger trigger) {
    StartEventBuilder startEventBuilder = null;
    if (trigger instanceof EventTrigger) {
      startEventBuilder = processBuilder.startEvent()
        .name(trigger.getName())
        .message(((EventTrigger) trigger).getPathPattern());
    } else if (trigger instanceof ManualTrigger) {

    } else if (trigger instanceof ScheduleTrigger) {

    }
    return startEventBuilder;
  }

  private ServiceTaskBuilder tasks(StartEventBuilder startEventBuilder, List<Task> tasks) {
    AtomicInteger index = new AtomicInteger(1);
    ServiceTaskBuilder serviceTaskBuilder = null;
    for (Task task : tasks) {
      Optional<AbstractRuntimeDelegate> delegate = delegates.stream()
        .filter(d -> d.fromTask().equals(task.getClass()))
        .findAny();
      if (delegate.isPresent()) {
        // TODO: use super class to avoid null check
        if (serviceTaskBuilder == null) {
          serviceTaskBuilder = startEventBuilder
            .serviceTask(task.id(index.getAndIncrement()))
            .name(task.getName())
            .camundaDelegateExpression(delegate.get().getExpression());
        } else {
          serviceTaskBuilder = serviceTaskBuilder
            .serviceTask(task.id(index.getAndIncrement()))
            .name(task.getName())
            .camundaDelegateExpression(delegate.get().getExpression());
        }
      } else {
        logger.warn("No delegate from {} found", task.getClass());
      }
    }
    return serviceTaskBuilder;
  }

  private void enhance(BpmnModelInstance model, List<Task> tasks) {
    AtomicInteger index = new AtomicInteger(1);
    tasks.stream()
      .filter(task -> delegates.stream().anyMatch(d -> d.fromTask().equals(task.getClass())))
      .forEach(task -> {
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

}

package org.folio.rest.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.CaseUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.delegate.AbstractWorkflowDelegate;
import org.folio.rest.model.Script;
import org.folio.rest.workflow.model.Branch;
import org.folio.rest.workflow.model.ConnectTo;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.Event;
import org.folio.rest.workflow.model.MessageCorrelationStartEvent;
import org.folio.rest.workflow.model.MoveToLastGateway;
import org.folio.rest.workflow.model.MoveToNode;
import org.folio.rest.workflow.model.Navigation;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.ParallelGateway;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.ScheduleStartEvent;
import org.folio.rest.workflow.model.ScriptType;
import org.folio.rest.workflow.model.SetupTask;
import org.folio.rest.workflow.model.StartEvent;
import org.folio.rest.workflow.model.Task;
//
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

  //@formatter:off
  private final static Class<?>[] SERIALIZABLE_TYPES = new Class<?>[] {
    String.class,
    Number.class,
    Boolean.class,
    Enum.class
  };
  // @formatter:on

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private List<AbstractWorkflowDelegate> workflowDelegates;

  public BpmnModelInstance fromWorkflow(Workflow workflow) {
    BpmnModelInstance model = build(workflow);
    expressions(model, workflow);
    return model;
  }

  private BpmnModelInstance build(Workflow workflow) {

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess().name(workflow.getName());

    List<Node> nodes = workflow.getNodes();

    if (workflow.getNodes().isEmpty()) {
      return processBuilder.done();
    }

    AbstractFlowNodeBuilder<?, ?> builder = processBuilder.startEvent();

    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);

      if (i == 0 && !(node instanceof StartEvent)) {
        // TODO: create custom exception and controller advice to handle better
        throw new RuntimeException("Workflow must start with a start event!");
      }

      if (node instanceof Event) {

        if (node instanceof StartEvent) {

          if (node instanceof ScheduleStartEvent) {
            builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName())
                .timerWithCycle(((ScheduleStartEvent) node).getChronExpression());
          } else if (node instanceof MessageCorrelationStartEvent) {
            builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName())
                .message(((MessageCorrelationStartEvent) node).getMessage());
          } else {
            // unknown start event
          }

        } else if (node instanceof EndEvent) {

          builder.endEvent();

        } else {
          // unknown event
        }

      } else if (node instanceof Task) {

        System.out.println("\n\n" + node.getClass().getSimpleName() + "\n\n");

        Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
            .filter(d -> d.fromTask().equals(node.getClass())).findAny();

        if (delegate.isPresent()) {

          builder = builder.serviceTask(node.getIdentifier()).name(node.getName())
              .camundaDelegateExpression(delegate.get().getExpression());

        } else {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

      } else if (node instanceof Branch) {

        if (node instanceof ExclusiveGateway) {

        } else if (node instanceof MoveToLastGateway) {

        } else if (node instanceof ParallelGateway) {

        } else if (node instanceof MoveToNode) {

        } else {
          // unknown branch
        }

        // must end in end event or connect to

      } else if (node instanceof Navigation) {

        if (node instanceof ConnectTo) {

          builder = builder.connectTo(((ConnectTo) node).getNodeId());

        } else {
          // unknown navigation
        }

      }

    }

    // must end in end event or connect to

    return builder.done();
  }

  private void expressions(BpmnModelInstance model, Workflow workflow) {

    List<Node> nodes = workflow.getNodes();

    nodes.stream().filter(node -> node instanceof Task).forEach(node -> {

      Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
          .filter(d -> d.fromTask().equals(node.getClass())).findAny();

      if (delegate.isPresent()) {

        ExtensionElements extensions = model.newInstance(ExtensionElements.class);

        if (node instanceof SetupTask) {

          if (((SetupTask) node).getLoadInitialContext()) {
            Map<String, String> initialContext = workflow.getInitialContext();
            CamundaField field = model.newInstance(CamundaField.class);
            field.setCamundaName("initialContext");
            try {
              field.setCamundaStringValue(objectMapper.writeValueAsString(initialContext));
            } catch (JsonProcessingException e) {
              logger.warn("Failed to serialize initial context");
            }
            extensions.addChildElement(field);
          }

          List<Script> processorScripts = getProcessorScripts(nodes);
          CamundaField field = model.newInstance(CamundaField.class);
          field.setCamundaName("processorScripts");
          try {
            field.setCamundaStringValue(objectMapper.writeValueAsString(processorScripts));
          } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize processor scripts");
          }
          extensions.addChildElement(field);

        } else {

          for (Field f : node.getClass().getDeclaredFields()) {
            f.setAccessible(true);

            CamundaField field = model.newInstance(CamundaField.class);
            field.setCamundaName(f.getName());

            try {
              if (isSerializableType(f.getType())) {
                field.setCamundaStringValue(f.get(node).toString());
              } else {
                field.setCamundaStringValue(objectMapper.writeValueAsString(f.get(node)));
              }
            } catch (JsonProcessingException e) {
              logger.warn("Failed to serialize field {} of node {}", f.getName(), node.getClass().getSimpleName());
            } catch (IllegalArgumentException e) {
              logger.warn("Unknown field {} of node {}", f.getName(), node.getClass().getSimpleName());
            } catch (IllegalAccessException e) {
              logger.warn("Cannot access field {} of node {}", f.getName(), node.getClass().getSimpleName());
            }
            extensions.addChildElement(field);
          }

        }

        System.out.println("\n\n" + node.getIdentifier() + "\n\n");

        ModelElementInstance element = model.getModelElementById(node.getIdentifier());
        element.addChildElement(extensions);

      } else {
        // TODO: create custom exception and controller advice to handle better
        throw new RuntimeException("Task must have delegate representation!");
      }

    });
  }

  private List<Script> getProcessorScripts(List<Node> nodes) {
    List<Script> scripts = new ArrayList<Script>();
    nodes.stream().filter(node -> node instanceof ProcessorTask).forEach(node -> {
      String name = CaseUtils.toCamelCase(((ProcessorTask) node).getName(), false, ' ');
      String code = ((ProcessorTask) node).getScript();
      ScriptType type = ((ProcessorTask) node).getScriptType();
      scripts.add(Script.of(name, code, type));
    });
    return scripts;
  }

  private boolean isSerializableType(Class<?> type) {
    for (Class<?> c : SERIALIZABLE_TYPES) {
      if (c.isAssignableFrom(type)) {
        return true;
      }
    }
    return false;
  }

}

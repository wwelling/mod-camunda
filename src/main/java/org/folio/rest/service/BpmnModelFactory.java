package org.folio.rest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.CaseUtils;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.delegate.AbstractWorkflowDelegate;
import org.folio.rest.model.Script;
import org.folio.rest.workflow.components.Branch;
import org.folio.rest.workflow.components.Conditional;
import org.folio.rest.workflow.components.Event;
import org.folio.rest.workflow.components.Navigation;
import org.folio.rest.workflow.components.StartEvent;
import org.folio.rest.workflow.components.Subprocess;
import org.folio.rest.workflow.components.Task;
import org.folio.rest.workflow.components.Wait;
import org.folio.rest.workflow.model.ConnectTo;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.EventSubprocess;
import org.folio.rest.workflow.model.ExclusiveGateway;
import org.folio.rest.workflow.model.MessageCorrelationStartEvent;
import org.folio.rest.workflow.model.MessageCorrelationWait;
import org.folio.rest.workflow.model.MoveToLastGateway;
import org.folio.rest.workflow.model.MoveToNode;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.ParallelGateway;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.ScheduleStartEvent;
import org.folio.rest.workflow.model.ScriptType;
import org.folio.rest.workflow.model.SignalStartEvent;
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

  private final static String SETUP_TASK_ID = "setup_task_98832611_3d33_476b_adcc_fcb6c4e8718b";

  // @formatter:off
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

    // @formatter:off
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess().name(workflow.getName())
        .camundaHistoryTimeToLive(workflow.getHistoryTimeToLive())
        .camundaVersionTag(workflow.getVersionTag());
    // @formatter:on

    BpmnModelInstance model = build(processBuilder, workflow);

    workflow.getNodes().stream().filter(node -> node instanceof Subprocess).forEach(subprocess -> {
      subprocess(processBuilder, subprocess);
    });

    setup(model, workflow);
    expressions(model, workflow.getNodes());
    return model;
  }

  private BpmnModelInstance build(ProcessBuilder processBuilder, Workflow workflow) {
    List<Node> nodes = workflow.getNodes();

    if (nodes.isEmpty()) {
      return processBuilder.done();
    }

    AbstractFlowNodeBuilder<?, ?> builder = processBuilder.startEvent();

    if (!(nodes.get(0) instanceof StartEvent)) {
      // TODO: create custom exception and controller advice to handle better
      throw new RuntimeException("Workflow must start with a start event!");
    }

    builder = build(builder, nodes, true);

    return builder.done();
  }

  private void subprocess(ProcessBuilder processBuilder, Node node) {
    AbstractFlowNodeBuilder<?, ?> builder = null;
    if (node instanceof EventSubprocess) {
      String identifier = node.getIdentifier();
      String name = node.getName();
      builder = processBuilder.eventSubProcess(identifier).name(name).startEvent();
    } else {
      // unknown subprocess
    }
    builder = build(builder, ((Subprocess) node).getNodes(), false);
  }

  private AbstractFlowNodeBuilder<?, ?> build(AbstractFlowNodeBuilder<?, ?> builder, List<Node> nodes, boolean setup) {

    for (Node node : nodes) {

      if (node instanceof Event) {
        if (node instanceof StartEvent) {

          if (((StartEvent) node).isAsyncBefore()) {
            builder = builder.camundaAsyncBefore();
          }

          boolean interrupting = ((StartEvent) node).isInterrupting();

          if (node instanceof ScheduleStartEvent) {
            builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName())
                .timerWithCycle(((ScheduleStartEvent) node).getChronExpression()).interrupting(interrupting);
          } else if (node instanceof SignalStartEvent) {
            builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName())
                .signal(((SignalStartEvent) node).getSignal()).interrupting(interrupting);
          } else if (node instanceof MessageCorrelationStartEvent) {
            builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName())
                .message(((MessageCorrelationStartEvent) node).getMessage()).interrupting(interrupting);
          } else {
            // unknown start event
          }

          if (setup) {
            // @formatter:off
            builder = builder.serviceTask(SETUP_TASK_ID).name("Setup")
                .camundaDelegateExpression("${setupDelegate}")
                .camundaAsyncAfter();
            // @formatter:on
          }

        } else if (node instanceof EndEvent) {
          builder = builder.endEvent();
        } else {
          // unknown event
        }
      } else if (node instanceof Task) {

        Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
            .filter(d -> d.fromTask().equals(node.getClass())).findAny();

        if (delegate.isPresent()) {
          builder = builder.serviceTask(node.getIdentifier()).name(node.getName())
              .camundaDelegateExpression(delegate.get().getExpression());
        } else {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

        if (((Task) node).isAsyncBefore()) {
          builder = builder.camundaAsyncBefore();
        }

        if (((Task) node).isAsyncAfter()) {
          builder = builder.camundaAsyncAfter();
        }

      } else if (node instanceof Branch) {

        if (node instanceof ExclusiveGateway) {
          builder = builder.exclusiveGateway().name(node.getName());
        } else if (node instanceof MoveToLastGateway) {
          builder = builder.moveToLastGateway();
        } else if (node instanceof ParallelGateway) {
          // TODO: implement and ensure validation
          throw new RuntimeException("Parallel gateway not yet supported!");
        } else if (node instanceof MoveToNode) {
          // TODO: implement and ensure validation
          throw new RuntimeException("Move to node not yet supported!");
        } else {
          // unknown branch
        }

        if (node instanceof Conditional) {
          builder = builder.condition(((Conditional) node).getAnswer(), ((Conditional) node).getCondition());
        }

        builder = build(builder, ((Branch) node).getNodes(), false);

      } else if (node instanceof Navigation) {

        if (node instanceof ConnectTo) {

          // TODO: figure out way to get identifier from id
          builder = builder.connectTo(((ConnectTo) node).getNodeId());

        } else {
          // unknown navigation
        }

      } else if (node instanceof Wait) {

        if (node instanceof MessageCorrelationWait) {

          builder = builder.receiveTask(((Node) node).getIdentifier()).name(((Node) node).getName())
              .message(((MessageCorrelationWait) node).getMessage());

        } else {
          // unknown wait
        }

        if (((Wait) node).isAsyncBefore()) {
          builder = builder.camundaAsyncBefore();
        }

        if (((Wait) node).isAsyncAfter()) {
          builder = builder.camundaAsyncAfter();
        }

      }
    }

    // must end in end event or connect to

    return builder;
  }

  private void setup(BpmnModelInstance model, Workflow workflow) {
    ExtensionElements extensions = model.newInstance(ExtensionElements.class);

    Map<String, String> initialContext = workflow.getInitialContext();
    CamundaField icField = model.newInstance(CamundaField.class);
    icField.setCamundaName("initialContext");
    try {
      icField.setCamundaStringValue(objectMapper.writeValueAsString(initialContext));
    } catch (JsonProcessingException e) {
      logger.warn("Failed to serialize initial context");
    }
    extensions.addChildElement(icField);

    List<Script> processorScripts = getProcessorScripts(workflow.getNodes());
    CamundaField psField = model.newInstance(CamundaField.class);
    psField.setCamundaName("processorScripts");
    try {
      psField.setCamundaStringValue(objectMapper.writeValueAsString(processorScripts));
    } catch (JsonProcessingException e) {
      logger.warn("Failed to serialize processor scripts");
    }
    extensions.addChildElement(psField);

    ModelElementInstance element = model.getModelElementById(SETUP_TASK_ID);
    element.addChildElement(extensions);
  }

  private void expressions(BpmnModelInstance model, List<Node> nodes) {
    nodes.stream().forEach(node -> {

      Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
          .filter(d -> d.fromTask().equals(node.getClass())).findAny();

      if (delegate.isPresent()) {

        ExtensionElements extensions = model.newInstance(ExtensionElements.class);

        FieldUtils.getAllFieldsList(delegate.get().getClass()).stream()
            .filter(df -> Expression.class.isAssignableFrom(df.getType()))
            .map(df -> FieldUtils.getDeclaredField(node.getClass(), df.getName(), true)).forEach(f -> {
              try {

                CamundaField field = model.newInstance(CamundaField.class);
                field.setCamundaName(f.getName());
                field.setCamundaStringValue(serialize(f.get(node)));

                extensions.addChildElement(field);

              } catch (JsonProcessingException | IllegalArgumentException | IllegalAccessException e) {
                // TODO: create custom exception and controller advice to handle better
                throw new RuntimeException(e);
              }
            });

        ModelElementInstance element = model.getModelElementById(node.getIdentifier());
        element.addChildElement(extensions);

      } else {

        if (node instanceof Branch) {
          expressions(model, ((Branch) node).getNodes());
        } else if (node instanceof Subprocess) {
          expressions(model, ((Subprocess) node).getNodes());
        } else if (node instanceof Task) {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

      }
    });
  }

  private List<Script> getProcessorScripts(List<Node> nodes) {
    List<Script> scripts = new ArrayList<Script>();
    nodes.stream().forEach(node -> {
      if (node instanceof ProcessorTask) {
        String name = CaseUtils.toCamelCase(((ProcessorTask) node).getName(), false, ' ');
        String code = ((ProcessorTask) node).getScript();
        ScriptType type = ((ProcessorTask) node).getScriptType();
        scripts.add(Script.of(name, code, type));
      } else if (node instanceof Branch) {
        scripts.addAll(getProcessorScripts(((Branch) node).getNodes()));
      } else if (node instanceof Subprocess) {
        scripts.addAll(getProcessorScripts(((Subprocess) node).getNodes()));
      }
    });
    return scripts;
  }

  private String serialize(Object value) throws JsonProcessingException {
    if (isSerializableType(value.getClass())) {
      return value.toString();
    } else {
      return objectMapper.writeValueAsString(value);
    }
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

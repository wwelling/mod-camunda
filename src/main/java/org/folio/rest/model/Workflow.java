package org.folio.rest.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.folio.rest.domain.model.AbstractBaseEntity;

@Entity
public class Workflow extends AbstractBaseEntity {

  @NotNull
  @Size(min = 4, max = 64)
  @Column(unique = true)
  private String name;

  @Column(unique = true)
  private String deploymentId;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> processDefinitionIds = new HashSet<String>();

  @Column
  private boolean active;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<Task> tasks;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  private Trigger startTrigger;

  public Workflow() {
    super();
    active = false;
    tasks = new ArrayList<Task>();
  }

  public Workflow(String name) {
    this();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Set<String> getProcessDefinitionIds() {
    return processDefinitionIds;
  }

  public void setProcessDefinitionIds(Set<String> processDefinitionIds) {
    this.processDefinitionIds = processDefinitionIds;
  }

  public void addProcessDefinitionId(String processDefinitionId) {
    if (!processDefinitionIds.contains(processDefinitionId)) {
      processDefinitionIds.add(processDefinitionId);
    }
  }

  public void clearProcessDefinitionIds() {
    processDefinitionIds.clear();
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }

  public Trigger getStartTrigger() {
    return startTrigger;
  }

  public void setStartTrigger(Trigger startTrigger) {
    this.startTrigger = startTrigger;
  }

}

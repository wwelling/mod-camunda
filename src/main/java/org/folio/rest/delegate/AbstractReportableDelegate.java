package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractReportableDelegate extends AbstractRuntimeDelegate {

  @Autowired
  protected StreamService streamService;

  protected Boolean isReporting = false;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    setReporting((Boolean) execution.getVariable("isReporting"));
  }

  protected void updateReport(String primaryStreamId, String entry) {
    if (isReporting) {
      streamService.appendToReport(primaryStreamId, entry);
    }
  }


  public Boolean isReporting() {
    return isReporting;
  }

  public void setReporting(Boolean isReporting) {
    this.isReporting = isReporting;
  }
}

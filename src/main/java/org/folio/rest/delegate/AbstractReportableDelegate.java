package org.folio.rest.delegate;

import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractReportableDelegate extends AbstractRuntimeDelegate {

  @Autowired
  protected StreamService streamService;

  protected void updateReport(String primaryStreamId, String entry) {
    streamService.appendToReport(primaryStreamId, entry);
  }
}

package org.folio.rest.service;

import org.folio.rest.jms.model.Event;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TransformEventService {

  private static final String CHECK_OUT_PATH = "/events/circulation/check-out-by-barcode";
  private static final String CHECK_IN_PATH = "/events/circulation/loans";

  public HashMap transformEventToProcessVariables(Event event) {

    // id, userId, itemId,
    HashMap hm = new HashMap();

    return hm;
  }

}

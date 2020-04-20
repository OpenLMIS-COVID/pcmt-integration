package org.openlmis.integration.pcmt.service.pcmt;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PcmtService {

  @Autowired
  private PcmtDataService pcmtDataService;

  private Logger logger;

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup() {
    getPayloadFromPcmt();
  }
  public Object getPayloadFromPcmt() {
    Object data = pcmtDataService
        .downloadData();
    return data;
  }

}

/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.integration.pcmt.service;

import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.pcmt.web.PcmtResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PayloadBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(PayloadBuilder.class);
  private static final XLogger X_LOGGER = XLoggerFactory.getXLogger(PayloadBuilder.class);

  @Autowired
  private PcmtDataService pcmtDataService;

  /**
   * This method retrieves Payload from PCTM.
   *
   * @return List of Products.
   */

  public Payload build(Object o) {

    Object unMappedPayload = getPayloadFromPcmt();
    //TODO map responseBody to payload.
    return null;
  }

  private PcmtResponseBody getPayloadFromPcmt() {
    return pcmtDataService.downloadData();
  }

  // this method is only for debug
  @EventListener(ApplicationReadyEvent.class)
  private void doSomethingAfterStartup() {
    getPayloadFromPcmt();
  }


}
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

import org.springframework.stereotype.Component;

@Component
public class PayloadBuilder {

  /**
   * This docs will be deleted.
   *
   */
  //TODO Change method to private and return Void
  public Payload build() {

    PcmtResponseBody payload = getPayloadFromPcmt();
    int xx = payload.getItemsCount()/100;
    System.out.println(payload.getItemsCount());
    for(int i = 0; i < xx; i++){
      System.out.println(i);
    }

//    //Sample object adding to queue
//    OrderableDto orderableDto = new OrderableDto();
//    addOjbectsToQueue(orderableDto);

    return null;
  }

  private PcmtResponseBody getPayloadFromPcmt() {
    return pcmtDataService.downloadData(1);
  }

  // this method is only for debug
  @EventListener(ApplicationReadyEvent.class)
  private void doSomethingAfterStartup() {
    build();
  }

}
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

import java.util.List;

import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.service.pcmt.dto.PcmtResponseBody;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayloadBuilder {

  @Autowired
  private PcmtDataService pcmtDataService;

  @Autowired
  private IntegrationExecutionService integrationExecutionService;

  /**
   * This docs will be deleted.
   *
   */
  public Payload build(Object o) {

    PcmtResponseBody responseBody = pcmtDataService.downloadData(1);
    List<Item> items = responseBody.getEmbedded().getItems();

    int xx = responseBody.getItemsCount() / 100;

    for (int i = 0; i <= xx; i++) {
      if (i > 0) {
        responseBody = pcmtDataService.downloadData(i + 1);
        items = responseBody.getEmbedded().getItems();
      }

      items.forEach((n) ->
          integrationExecutionService.addObjectsToQueue(mapItemToDto(n))
      );
    }

    return null;
  }

  private OrderableDto mapItemToDto(Item item) {
    OrderableDto orderableDto = new OrderableDto();

    //orderableDto.setId(UUID.fromString(item.getValues().getLmisUuid().get(0).getData()));
    //orderableDto.setFullProductName(item.getValues().getProductDescription().get(0).getData());
    //orderableDto.setProductCode(item.getValues().getLmisCode().get(0).getData());
    //    orderableDto.setNetContent(
    //        Long.parseLong(
    //            item.getValues().getUomQtyFactor().get(0).getData()));
    //orderableDto.setDescription(item.getValues().getProductDescription().get(0).getData());

    return orderableDto;
  }

  //    @EventListener(ApplicationReadyEvent.class)
  //    private void doSomethingAfterStartup() {
  //      build(null);
  //    }

}
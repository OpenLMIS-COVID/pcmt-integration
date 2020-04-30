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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.UUID;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.service.pcmt.dto.Values;
import org.openlmis.integration.pcmt.service.referencedata.orderable.DispensableDto;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;

public final class OrderableBuilder {

  /**
   * Maps Item to OrderableDto.
   *
   */
  public static OrderableDto build(Item item, Long uomQtyFactor) {
    OrderableDto orderableDto = new OrderableDto();
    DispensableDto dispensableDto =
        new DispensableDto(item.getValues().getBaseUom().get(0).getData(), null, null, null);
    Values values = item.getValues();
    if (isNotEmpty(values.getLmisUuid())) {
      orderableDto.setId(UUID.fromString(values.getLmisUuid().get(0).getData()));
    }
    orderableDto.setDispensable(dispensableDto);
    orderableDto.setFullProductName(item.getValues().getProductDescription().get(0).getData());
    orderableDto.setProductCode(item.getValues().getLmisCode().get(0).getData());
    orderableDto.setNetContent(uomQtyFactor);
    orderableDto.setDescription(item.getValues().getProductDescription().get(0).getData());
    orderableDto.setPackRoundingThreshold(
        Long.valueOf(item.getValues().getLmisPackRoundingThreshold().get(0).getData()));
    orderableDto.setRoundToZero(
        Boolean.getBoolean(item.getValues().getLmisRoundToZero().get(0).getData()));
    return orderableDto;
  }

  private OrderableBuilder() {
  }
}

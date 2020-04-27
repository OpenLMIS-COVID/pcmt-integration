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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.openlmis.integration.pcmt.testbuilder.ItemDataBuilder;
import org.openlmis.integration.pcmt.testbuilder.ValuesDataBuilder;

public class OrderableBuilderTest {

  private static final Long UOM_QTY_FACTOR = 10L;

  private Item item;

  @Before
  public void setUp() {
    item = new ItemDataBuilder().build();
  }

  @Test
  public void shouldBuildOrderableDtoFromItem() {
    OrderableDto orderableDto = OrderableBuilder.build(item, UOM_QTY_FACTOR);

    assertEquals(orderableDto.getId(),
        UUID.fromString(item.getValues().getLmisUuid().get(0).getData()));
    assertEquals(orderableDto.getProductCode(),
        item.getValues().getLmisCode().get(0).getData());
    assertEquals(orderableDto.getFullProductName(),
        item.getValues().getProductDescription().get(0).getData());
    assertEquals(orderableDto.getDescription(),
        item.getValues().getProductDescription().get(0).getData());
    assertEquals(orderableDto.getNetContent(), UOM_QTY_FACTOR);
  }

  @Test
  public void shouldBuildOrderableDtoFromItemWithoutLmisUuid() {
    item = new ItemDataBuilder().withValues(
        new ValuesDataBuilder().withoutLmisUuid().build()).build();

    OrderableDto orderableDto = OrderableBuilder.build(item, UOM_QTY_FACTOR);

    assertNull(orderableDto.getId());
    assertEquals(orderableDto.getProductCode(),
        item.getValues().getLmisCode().get(0).getData());
    assertEquals(orderableDto.getFullProductName(),
        item.getValues().getProductDescription().get(0).getData());
    assertEquals(orderableDto.getDescription(),
        item.getValues().getProductDescription().get(0).getData());
    assertEquals(orderableDto.getNetContent(), UOM_QTY_FACTOR);
  }
}

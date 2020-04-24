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

package org.openlmis.integration.pcmt.service.fetch;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openlmis.integration.pcmt.service.OrderableBuilder;
import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.service.pcmt.dto.PcmtResponseBody;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;

public class OrderableIntegrationFetchTask extends IntegrationFetchTask<OrderableDto> {

  private final PcmtDataService pcmtDataService;
  private final BlockingQueue<OrderableDto> queue;
  private final ZonedDateTime executionTime;
  private int page;

  protected PcmtDataService getPcmtDataService() {
    return pcmtDataService;
  }

  @Override
  protected BlockingQueue<OrderableDto> getQueue() {
    return queue;
  }

  @Override
  protected ZonedDateTime getExecutionTime() {
    return executionTime;
  }

  @Override
  protected int getPageNumber() {
    return page;
  }

  @Override
  protected void incPage() {
    page++;
  }

  /**
   * Constructor of OrderableIntegrationFetchTask.
   *
   */
  public OrderableIntegrationFetchTask(
      PcmtDataService pcmtDataService,
      BlockingQueue<OrderableDto> queue,
      Clock clock) {
    this.pcmtDataService = pcmtDataService;
    this.queue = queue;
    this.executionTime = ZonedDateTime.now(clock);
    this.page = 1;
  }

  @Override
  public void run() {
    List<Item> items;
    do {
      PcmtResponseBody responseBody = getPcmtDataService().downloadData(getPageNumber());
      items = responseBody.getEmbedded().getItems();

      for (Item item : items) {
        OrderableDto entity = OrderableBuilder.build(item);
        addToQueue(entity);
      }

    } while (nextPage(items));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof OrderableIntegrationFetchTask)) {
      return false;
    }

    OrderableIntegrationFetchTask that = (OrderableIntegrationFetchTask) o;

    return new EqualsBuilder()
        .append(getExecutionTime(), that.getExecutionTime())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 49)
        .append(getExecutionTime())
        .toHashCode();
  }
}

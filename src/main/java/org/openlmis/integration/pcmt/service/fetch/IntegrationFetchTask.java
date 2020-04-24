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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.collections4.CollectionUtils;
import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.web.BaseDto;
import org.slf4j.Logger;

public abstract class IntegrationFetchTask<T extends BaseDto> implements Runnable,
    Comparable<IntegrationFetchTask<T>> {

  protected abstract Logger getLogger();

  protected abstract PcmtDataService getPcmtDataService();

  protected abstract BlockingQueue<T> getQueue();

  protected abstract ZonedDateTime getExecutionTime();

  protected abstract int getPageNumber();

  protected abstract void incPage();

  protected void addToQueue(T entity) {
    getQueue().add(entity);
    getLogger().debug("Added fetched entity with id {} to queue.", entity.getId());
  }

  protected boolean nextPage(List<Item> items) {
    if (CollectionUtils.isNotEmpty(items)) {
      getLogger().debug("Fetched {} with {} items", getPageNumber(), items.size());
      incPage();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(IntegrationFetchTask<T> other) {
    return this.getExecutionTime().compareTo(other.getExecutionTime());
  }
}

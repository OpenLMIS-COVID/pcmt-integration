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

package org.openlmis.integration.pcmt.service.send;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;

final class SendTestTask extends OrderableIntegrationSendTask {

  private static final int AWAIT_TIME = 3;
  private final ZonedDateTime executionTime;

  @Getter
  private boolean executed;

  public SendTestTask(boolean manualExecution, ZonedDateTime executionTime) {
    super(null, null, null,
        null, manualExecution, null,
        Clock.fixed(Instant.now(), ZoneOffset.UTC), null, null);
    this.executionTime = executionTime;
  }

  @Override
  protected ZonedDateTime getExecutionTime() {
    return executionTime;
  }

  @Override
  public void run() {
    send(null);
  }

  @Override
  protected ExecutionResponse send(OrderableDto entity) {
    try {
      TimeUnit.SECONDS.sleep(AWAIT_TIME);
    } catch (InterruptedException exp) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(exp);
    } finally {
      executed = true;
    }
    return null;
  }
}

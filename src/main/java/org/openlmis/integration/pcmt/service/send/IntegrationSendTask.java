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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import java.time.Clock;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.Payload;
import org.openlmis.integration.pcmt.service.PayloadBuilder;
import org.springframework.http.RequestEntity;

public abstract class IntegrationSendTask<T> implements Runnable,
    Comparable<IntegrationSendTask<T>> {

  protected abstract BlockingQueue<T> getQueue();

  protected abstract Integration getIntegration();

  protected abstract UUID getUserId();

  protected abstract ExecutionRepository getExecutionRepository();

  protected abstract ZonedDateTime getExecutionTime();

  protected abstract boolean isManualExecution();

  protected abstract Clock getClock();

  protected abstract PayloadBuilder getPayloadBuilder();

  protected abstract ObjectMapper getObjectMapper();

  public abstract boolean equals(Object o);

  public abstract int hashCode();

  protected abstract RequestEntity<T> initRequest(T entity) throws URISyntaxException;

  protected abstract ExecutionResponse send(T entity) throws InterruptedException;

  protected Execution initExecution() {
    Execution execution;
    if (isManualExecution()) {
      execution = Execution.forManualExecutionV2(getIntegration(), getUserId(), getClock());
    } else {
      execution = Execution.forAutomaticExecutionV2(getIntegration(), getClock());
    }

    return getExecutionRepository().saveAndFlush(execution);
  }

  protected Execution addRequestToExecution(T entity, Execution execution) {
    try {
      Payload payload = getPayloadBuilder().build(entity);
      String requestBody = getObjectMapper().writeValueAsString(payload);
      execution.setRequestBody(requestBody);
    } catch (Exception exp) {
      throw new IllegalStateException(exp);
    }

    return getExecutionRepository().saveAndFlush(execution);
  }

  protected Execution addResponseToExecution(ExecutionResponse response, Execution execution) {
    execution.markAsDone(response, getClock());
    return getExecutionRepository().saveAndFlush(execution);
  }

  @Override
  public void run() {
    try {
      while (true) {
        T entity = getQueue().take();
        Execution execution = initExecution();
        execution = addRequestToExecution(entity, execution);
        ExecutionResponse response = send(entity);
        addResponseToExecution(response, execution);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
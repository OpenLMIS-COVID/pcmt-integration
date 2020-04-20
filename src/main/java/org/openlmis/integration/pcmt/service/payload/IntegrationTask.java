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

package org.openlmis.integration.pcmt.service.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.Payload;
import org.openlmis.integration.pcmt.service.PayloadBuilder;

public abstract class IntegrationTask<T> implements Runnable, Comparable<IntegrationTask<T>> {

  protected abstract T getEntity();

  protected abstract void setEntity(T entity);

  protected abstract ExecutableRequest<T> getRequest();

  protected abstract void setRequest(ExecutableRequest<T> request);

  protected abstract Integration getIntegration();

  protected abstract void setIntegration(Integration integration);

  protected abstract UUID getUserId();

  protected abstract Execution getExecution();

  protected abstract void setExecution(Execution execution);

  protected abstract ExecutionRepository getExecutionRepository();

  protected abstract Clock getClock();

  protected abstract PayloadBuilder getPayloadBuilder();

  protected abstract ObjectMapper getObjectMapper();

  protected abstract ZonedDateTime getExecutionTime();

  protected abstract ExecutionResponse send();

  public abstract boolean equals(Object o);

  public abstract int hashCode();

  protected void initExecution() {
    if (getRequest().isManualExecution()) {
      setExecution(Execution.forManualExecutionV2(getIntegration(), getUserId(), getClock()));
    } else {
      setExecution(Execution.forAutomaticExecutionV2(getIntegration(), getClock()));
    }

    setExecution(getExecutionRepository().saveAndFlush(getExecution()));
  }

  protected void addRequestToExecution() {
    try {
      // TODO COV-29: create new PayloadBuilder build(getEntity());
      Payload payload = getPayloadBuilder().build(null);
      String requestBody = getObjectMapper().writeValueAsString(payload);
      getExecution().setRequestBody(requestBody);
    } catch (Exception exp) {
      throw new IllegalStateException(exp);
    }

    setExecution(getExecutionRepository().saveAndFlush(getExecution()));
  }

  protected void addResponseToExecution(ExecutionResponse response) {
    getExecution().markAsDone(response, getClock());
    setExecution(getExecutionRepository().saveAndFlush(getExecution()));
  }

  @Override
  public void run() {
    initExecution();
    addRequestToExecution();
    ExecutionResponse response = send();
    addResponseToExecution(response);
  }
}

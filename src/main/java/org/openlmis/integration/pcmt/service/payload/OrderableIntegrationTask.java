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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.PayloadBuilder;
import org.openlmis.integration.pcmt.service.referencedata.OrderableDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class OrderableIntegrationTask extends IntegrationTask<OrderableDto> {

  private OrderableDto entity;
  private ExecutableRequest<OrderableDto> request;
  private Integration integration;
  private final UUID userId;

  private Execution execution;
  private final ZonedDateTime executionTime;
  private final ExecutionRepository executionRepository;

  private final Clock clock;
  private final PayloadBuilder payloadBuilder;
  private final ObjectMapper objectMapper;

  private final RestTemplate restTemplate;

  /**
   * Constructor of OrderableIntegrationTask.
   */
  public OrderableIntegrationTask(
      OrderableDto entity,
      ExecutableRequest<OrderableDto> request,
      Integration integration, UUID userId,
      ExecutionRepository executionRepository, Clock clock,
      PayloadBuilder payloadBuilder, ObjectMapper objectMapper) {
    this.entity = entity;
    this.request = request;
    this.integration = integration;
    this.userId = userId;
    this.execution = null;
    this.executionTime = ZonedDateTime.now(clock);
    this.executionRepository = executionRepository;
    this.clock = clock;
    this.payloadBuilder = payloadBuilder;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
  }

  /**
   * Sends entity to the system in order to integrate it.
   *
   * @return execution response
   */
  @Override
  public ExecutionResponse send() {
    try {
      ResponseEntity<OrderableDto> response = restTemplate.exchange(
          request.getRequest(), OrderableDto.class);

      return new ExecutionResponse(ZonedDateTime.now(clock), response.getStatusCodeValue(),
          response.getBody().toString());
    } catch (RestClientResponseException exp) {
      return new ExecutionResponse(ZonedDateTime.now(clock), exp.getRawStatusCode(),
          exp.getResponseBodyAsString());
    } catch (Exception exp) {
      return new ExecutionResponse(ZonedDateTime.now(clock), INTERNAL_SERVER_ERROR.value(),
          exp.getMessage());
    }
  }

  @Override
  protected OrderableDto getEntity() {
    return entity;
  }

  @Override
  protected void setEntity(OrderableDto entity) {
    this.entity = entity;
  }

  @Override
  protected ExecutableRequest<OrderableDto> getRequest() {
    return request;
  }

  @Override
  protected void setRequest(
      ExecutableRequest<OrderableDto> request) {
    this.request = request;
  }

  @Override
  protected Integration getIntegration() {
    return integration;
  }

  @Override
  protected void setIntegration(Integration integration) {
    this.integration = integration;
  }

  @Override
  protected UUID getUserId() {
    return userId;
  }

  @Override
  protected Execution getExecution() {
    return execution;
  }

  @Override
  protected void setExecution(Execution execution) {
    this.execution = execution;
  }

  @Override
  protected ExecutionRepository getExecutionRepository() {
    return executionRepository;
  }

  @Override
  protected Clock getClock() {
    return clock;
  }

  @Override
  protected PayloadBuilder getPayloadBuilder() {
    return payloadBuilder;
  }

  @Override
  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  protected RestTemplate getRestTemplate() {
    return restTemplate;
  }

  @Override
  protected ZonedDateTime getExecutionTime() {
    return executionTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof OrderableIntegrationTask)) {
      return false;
    }

    OrderableIntegrationTask that = (OrderableIntegrationTask) o;

    return new EqualsBuilder()
        .append(getRequest().isManualExecution(), that.getRequest().isManualExecution())
        .append(getExecutionTime(), that.getExecutionTime())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getRequest().isManualExecution())
        .append(getExecutionTime())
        .toHashCode();
  }

  @Override
  public int compareTo(IntegrationTask<OrderableDto> other) {
    if (this.getRequest().isManualExecution() == other.getRequest().isManualExecution()) {
      return this.getExecutionTime().compareTo(other.getExecutionTime());
    }

    return getRequest().isManualExecution() ? 1 : -1;
  }
}

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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.PayloadBuilder;
import org.openlmis.integration.pcmt.service.referencedata.OrderableDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings({"PMD.PreserveStackTrace"})
public class OrderableIntegrationSendTask extends IntegrationSendTask<OrderableDto> {

  @Value("${service.url}")
  @Getter
  private String serviceUrl;
  private final BlockingQueue<OrderableDto> queue;
  private final Integration integration;
  private final UUID userId;
  private final ExecutionRepository executionRepository;
  private final boolean manualExecution;
  private final ZonedDateTime executionTime;
  private final Clock clock;
  private final PayloadBuilder payloadBuilder;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  @Override
  protected BlockingQueue<OrderableDto> getQueue() {
    return queue;
  }

  @Override
  protected Integration getIntegration() {
    return integration;
  }

  @Override
  protected UUID getUserId() {
    return userId;
  }

  @Override
  protected ExecutionRepository getExecutionRepository() {
    return executionRepository;
  }

  @Override
  protected boolean isManualExecution() {
    return manualExecution;
  }

  @Override
  protected ZonedDateTime getExecutionTime() {
    return executionTime;
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


  @Override
  protected RequestEntity<OrderableDto> initRequest(OrderableDto entity) throws URISyntaxException {
    return new RequestEntity<>(entity, HttpMethod.PUT,
        new URI(serviceUrl + "/api/orderables/" + entity.getId()));
  }

  /**
   * Sends entity to the system in order to integrate it.
   *
   * @return execution response
   */
  @Override
  protected ExecutionResponse send(OrderableDto entity) throws InterruptedException {
    try {
      RequestEntity<OrderableDto> request = initRequest(entity);
      ResponseEntity<OrderableDto> response = getRestTemplate().exchange(request,
          OrderableDto.class);

      return new ExecutionResponse(ZonedDateTime.now(getClock()), response.getStatusCodeValue(),
          response.getBody().toString());
    } catch (URISyntaxException e) {
      throw new InterruptedException(e.getMessage());
    } catch (RestClientResponseException e) {
      return new ExecutionResponse(ZonedDateTime.now(getClock()), e.getRawStatusCode(),
          e.getResponseBodyAsString());
    } catch (Exception e) {
      return new ExecutionResponse(ZonedDateTime.now(getClock()), INTERNAL_SERVER_ERROR.value(),
          e.getMessage());
    }
  }

  protected RestTemplate getRestTemplate() {
    return restTemplate;
  }

  /**
   * Constructor of OrderableIntegrationTask.
   */
  public OrderableIntegrationSendTask(
      BlockingQueue<OrderableDto> queue,
      Integration integration, UUID userId,
      ExecutionRepository executionRepository, boolean manualExecution,
      Clock clock, PayloadBuilder payloadBuilder, ObjectMapper objectMapper) {
    this.queue = queue;
    this.integration = integration;
    this.userId = userId;
    this.executionRepository = executionRepository;
    this.manualExecution = manualExecution;
    this.clock = clock;
    this.payloadBuilder = payloadBuilder;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
    this.executionTime = ZonedDateTime.now(getClock());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof OrderableIntegrationSendTask)) {
      return false;
    }

    OrderableIntegrationSendTask that = (OrderableIntegrationSendTask) o;

    return new EqualsBuilder()
        .append(getExecutionTime(), that.getExecutionTime())
        .append(isManualExecution(), that.isManualExecution())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getExecutionTime())
        .append(isManualExecution())
        .toHashCode();
  }

  @Override
  public int compareTo(IntegrationSendTask<OrderableDto> other) {
    if (this.isManualExecution() == other.isManualExecution()) {
      return this.getExecutionTime().compareTo(other.getExecutionTime());
    }

    return isManualExecution() ? 1 : -1;
  }
}

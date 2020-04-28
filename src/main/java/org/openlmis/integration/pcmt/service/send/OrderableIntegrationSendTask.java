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

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class OrderableIntegrationSendTask extends IntegrationSendTask<OrderableDto> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderableIntegrationSendTask.class);
  private static final String API_PATH = "/api/orderables/";

  private final BlockingQueue<OrderableDto> queue;
  private final Integration integration;
  private final UUID userId;
  private final String targetUrl;
  private final boolean manualExecution;
  private final ZonedDateTime executionTime;

  private final ExecutionRepository executionRepository;
  private final Clock clock;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
  private final AuthService authService;

  /**
   * Constructor of OrderableIntegrationTask.
   */
  public OrderableIntegrationSendTask(BlockingQueue<OrderableDto> queue,
      Integration integration, UUID userId, String targetUrl, boolean manualExecution,
      ExecutionRepository executionRepository, Clock clock,
      ObjectMapper objectMapper, AuthService authService) {
    this.queue = queue;
    this.integration = integration;
    this.userId = userId;
    this.targetUrl = targetUrl;
    this.manualExecution = manualExecution;
    this.executionRepository = executionRepository;
    this.clock = clock;
    this.objectMapper = objectMapper;
    this.authService = authService;
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
    return new HashCodeBuilder(11, 47)
        .append(getExecutionTime())
        .append(isManualExecution())
        .toHashCode();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

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
  protected String getTargetUrl() {
    return targetUrl;
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
  protected ExecutionRepository getExecutionRepository() {
    return executionRepository;
  }

  @Override
  protected Clock getClock() {
    return clock;
  }

  @Override
  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  protected HttpHeaders createInitHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + getToken());
    return headers;
  }

  @Override
  protected ExecutionResponse send(OrderableDto entity) {
    try {
      ResponseEntity<OrderableDto> response = tryToSend(entity);
      getLogger().debug("Updated Orderable with result code: {}", response.getStatusCode());
      return new ExecutionResponse(ZonedDateTime.now(getClock()), response.getStatusCodeValue(),
          response.getBody().toString());
    } catch (RestClientResponseException e) {
      return new ExecutionResponse(ZonedDateTime.now(getClock()), e.getRawStatusCode(),
          e.getResponseBodyAsString());
    } catch (URISyntaxException e) {
      return new ExecutionResponse(ZonedDateTime.now(getClock()), NOT_FOUND.value(),
          e.getMessage());
    }
  }

  protected RestTemplate getRestTemplate() {
    return restTemplate;
  }

  protected String getToken() {
    return authService.obtainAccessToken();
  }

  private RequestEntity<OrderableDto> initRequest(OrderableDto entity, URI uri) {
    return new RequestEntity<>(entity, createInitHeaders(), HttpMethod.PUT, uri);
  }

  private OrderableDto get(UUID entityId) throws URISyntaxException {
    URI uri = new URI(getTargetUrl() + API_PATH + entityId);
    RequestEntity<OrderableDto> req = new RequestEntity<>(createInitHeaders(), HttpMethod.GET, uri);
    ResponseEntity<OrderableDto> fetched;
    try {
      fetched = getRestTemplate().exchange(req, OrderableDto.class);
      return fetched.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      } else {
        throw e;
      }
    }
  }

  private ResponseEntity<OrderableDto> update(OrderableDto entity) throws URISyntaxException {
    URI uri = new URI(getTargetUrl() + API_PATH + entity.getId());
    RequestEntity<OrderableDto> req = initRequest(entity, uri);
    return getRestTemplate().exchange(req, OrderableDto.class);
  }

  private ResponseEntity<OrderableDto> create(OrderableDto entity) throws URISyntaxException {
    URI uri = new URI(getTargetUrl() + API_PATH);
    RequestEntity<OrderableDto> req = initRequest(entity, uri);
    return getRestTemplate().exchange(req, OrderableDto.class);
  }

  private ResponseEntity<OrderableDto> tryToSend(OrderableDto entity) throws URISyntaxException {
    OrderableDto dto = get(entity.getId());
    if (dto != null) {
      entity.getPrograms().addAll(dto.getPrograms());
      return update(entity);
    } else {
      return create(entity);
    }
  }
}

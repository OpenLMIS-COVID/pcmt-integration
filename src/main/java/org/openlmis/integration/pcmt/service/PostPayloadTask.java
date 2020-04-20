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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.pcmt.service.referencedata.ProgramReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("PMD.TooManyMethods")
public class PostPayloadTask implements Runnable, Comparable<PostPayloadTask> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostPayloadTask.class);

  private final ProgramReferenceDataService programReferenceDataService;
  private final ExecutionRepository executionRepository;
  private final PayloadBuilder payloadBuilder;
  private final ObjectMapper objectMapper;
  private final Clock clock;
  private final RestTemplate restTemplate;

  private final ZonedDateTime executionTime;
  private final PayloadRequest payloadRequest;

  /**
   * Creates a new instance.
   */
  public PostPayloadTask(ProgramReferenceDataService programReferenceDataService,
      ExecutionRepository executionRepository, PayloadBuilder payloadBuilder,
      ObjectMapper objectMapper, Clock clock, RestTemplate restTemplate,
      PayloadRequest payloadRequest) {
    this.programReferenceDataService = programReferenceDataService;
    this.executionRepository = executionRepository;
    this.payloadBuilder = payloadBuilder;
    this.objectMapper = objectMapper;
    this.clock = clock;
    this.restTemplate = restTemplate;

    this.executionTime = ZonedDateTime.now(clock);
    this.payloadRequest = payloadRequest;
  }

  @Override
  public void run() {
    LOGGER.info("Handle payload request: {}", payloadRequest);

    Profiler profiler = new Profiler("POST_PAYLOAD");
    profiler.setLogger(LOGGER);

    Execution execution = createExecution(payloadRequest, profiler);
    String requestBody = createRequestBody(payloadRequest, execution, profiler);
    sendRequestBody(payloadRequest, execution, requestBody, profiler);

    profiler.stop().log();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PostPayloadTask)) {
      return false;
    }

    PostPayloadTask that = (PostPayloadTask) o;

    return new EqualsBuilder()
        .append(payloadRequest.isManualExecution(), that.payloadRequest.isManualExecution())
        .append(executionTime, that.executionTime)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(payloadRequest.isManualExecution())
        .append(executionTime)
        .toHashCode();
  }

  @Override
  public int compareTo(PostPayloadTask other) {
    if (this.payloadRequest.isManualExecution() == other.payloadRequest.isManualExecution()) {
      return this.executionTime.compareTo(other.executionTime);
    }

    return payloadRequest.isManualExecution() ? 1 : -1;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
        .append("manualExecution", payloadRequest.isManualExecution())
        .append("executionTime", executionTime)
        .toString();
  }

  /**
   * Exports the current task state.
   */
  public void export(Exporter exporter) {
    exporter.setExecutionTime(executionTime);
    exporter.setProcessingPeriodId(payloadRequest.getPeriod().getId());
    exporter.setUserId(payloadRequest.getUserId());
    exporter.setDescription(payloadRequest.getDescription());
  }

  private Execution createExecution(PayloadRequest payloadRequest, Profiler profiler) {
    profiler.start("CREATE_EXECUTION");
    Execution execution = payloadRequest.createExecution(clock);

    profiler.start("SAVE_TO_DB");
    executionRepository.saveAndFlush(execution);

    return execution;
  }

  private String createRequestBody(PayloadRequest payloadRequest, Execution execution,
      Profiler profiler) {
    try {
      profiler.start("CREATE_PAYLOAD");
      Payload payload = createPayload(payloadRequest);

      profiler.start("CONVERT_PAYLOAD_TO_JSON");
      String requestBody = objectMapper.writeValueAsString(payload);

      profiler.start("SET_REQUEST_BODY");
      execution.setRequestBody(requestBody);

      profiler.start("UPDATE_EXECUTION");
      executionRepository.saveAndFlush(execution);

      return requestBody;
    } catch (Exception exp) {
      throw new IllegalStateException(exp);
    }
  }

  private Payload createPayload(PayloadRequest request) {
    String programName = getProgramName(request);
    ProcessingPeriodDto period = request.getPeriod();

    return payloadBuilder
        .build(null);
  }

  private String getProgramName(PayloadRequest request) {
    if (null == request.getProgramId()) {
      return null;
    }

    return programReferenceDataService
        .findOne(request.getProgramId())
        .getName();
  }

  private void sendRequestBody(PayloadRequest payloadRequest, Execution execution,
      String requestBody, Profiler profiler) {
    profiler.start("SEND_PAYLOAD");
    ExecutionResponse response = sendPayload(payloadRequest, requestBody);

    profiler.start("MARK_AS_DONE");
    execution.markAsDone(response, clock);

    profiler.start("UPDATE_EXECUTION");
    executionRepository.saveAndFlush(execution);

    LOGGER.info("Response status: {}; Message: {}", response.getStatusCode(), response.getBody());
  }

  private ExecutionResponse sendPayload(PayloadRequest request, String body) {
    try {
      RequestHeaders headers = setHeaders(request);
      HttpEntity<String> entity = RequestHelper.createEntity(headers, body);

      ResponseEntity<String> response = restTemplate
          .exchange(request.getTargetUrl(), HttpMethod.POST, entity, String.class);

      return new ExecutionResponse(ZonedDateTime.now(clock), response.getStatusCodeValue(),
          response.getBody());
    } catch (RestClientResponseException exp) {
      return new ExecutionResponse(ZonedDateTime.now(clock), exp.getRawStatusCode(),
          exp.getResponseBodyAsString());
    } catch (Exception exp) {
      return new ExecutionResponse(ZonedDateTime.now(clock), INTERNAL_SERVER_ERROR.value(),
          exp.getMessage());
    }
  }

  private RequestHeaders setHeaders(PayloadRequest request) {
    return RequestHeaders
        .init()
        .set(HttpHeaders.AUTHORIZATION, request.getAuthorizationHeader())
        .set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
  }

  public interface Exporter {

    void setExecutionTime(ZonedDateTime executionTime);

    void setProcessingPeriodId(UUID processingPeriodId);

    void setUserId(UUID userId);

    void setDescription(String description);

  }
}

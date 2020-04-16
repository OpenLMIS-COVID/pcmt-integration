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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.pcmt.service.referencedata.ProgramDto;
import org.openlmis.integration.pcmt.service.referencedata.ProgramReferenceDataService;
import org.openlmis.integration.pcmt.web.ExecutionDto;
import org.openlmis.integration.pcmt.web.ExecutionResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class PostPayloadTaskTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneOffset.UTC);

  private static final UUID PROGRAM_ID = UUID.randomUUID();
  private static final String PROGRAM_NAME = "program";

  private static final LocalDate START_DATE = LocalDate.of(2019, 1, 1);
  private static final LocalDate END_DATE = LocalDate.of(2019, 1, 31);

  private static final String TARGET_URL = "http://localhost";

  private static final ProgramDto PROGRAM = new ProgramDto("code", PROGRAM_NAME, null,
      null, null, null);
  private static final ProcessingPeriodDto PERIOD = new ProcessingPeriodDto(
      null, null, null, START_DATE, END_DATE);

  private static final Payload PAYLOAD = new Payload(Sets.newHashSet(), LocalDate.now(CLOCK));

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private ExecutionRepository executionRepository;

  @Mock
  private PayloadBuilder payloadBuilder;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private PayloadRequest automaticPayloadRequest;

  @Mock
  private PayloadRequest manualPayloadRequest;


  @Mock
  private ResponseEntity<String> responseEntity;

  private Execution execution = new Execution();

  @Before
  public void setUp() throws JsonProcessingException {
    given(automaticPayloadRequest.isManualExecution()).willReturn(false);
    given(automaticPayloadRequest.createExecution(CLOCK)).willReturn(execution);
    given(automaticPayloadRequest.getPeriod()).willReturn(PERIOD);
    given(automaticPayloadRequest.getTargetUrl()).willReturn(TARGET_URL);

    given(manualPayloadRequest.isManualExecution()).willReturn(true);
    given(manualPayloadRequest.createExecution(CLOCK)).willReturn(execution);
    given(manualPayloadRequest.getPeriod()).willReturn(PERIOD);
    given(manualPayloadRequest.getProgramId()).willReturn(PROGRAM_ID);
    given(manualPayloadRequest.getTargetUrl()).willReturn(TARGET_URL);

    given(programReferenceDataService.findOne(PROGRAM_ID)).willReturn(PROGRAM);

    given(payloadBuilder.build(START_DATE, END_DATE, PROGRAM_NAME, null)).willReturn(PAYLOAD);

    given(objectMapper.writeValueAsString(anyString())).willReturn("{}");

    given(restTemplate.exchange(eq(TARGET_URL), eq(HttpMethod.POST),
        any(HttpEntity.class), eq(String.class)))
        .willReturn(responseEntity);

    given(responseEntity.getStatusCodeValue()).willReturn(200);
    given(responseEntity.getBody()).willReturn("success");
  }

  @Test
  public void automaticTasksShouldBeBeforeManualTasks() {
    // given
    PostPayloadTask automaticTask = createTask(CLOCK, automaticPayloadRequest);
    PostPayloadTask manualTask = createTask(CLOCK, manualPayloadRequest);

    // when
    BlockingQueue<PostPayloadTask> queue = new PriorityBlockingQueue<>();
    queue.add(manualTask);
    queue.add(automaticTask);

    // then
    assertThat(queue.poll()).isEqualTo(automaticTask);
    assertThat(queue.poll()).isEqualTo(manualTask);
  }

  @Test
  public void oldTasksShouldBeBeforeNewTasks() {
    // given
    PostPayloadTask oldTask = createTask(
        Clock.fixed(ZonedDateTime.now().minusDays(7).toInstant(), ZoneOffset.UTC),
        automaticPayloadRequest);
    PostPayloadTask newTask = createTask(
        Clock.fixed(Instant.now(), ZoneOffset.UTC),
        automaticPayloadRequest);

    // when
    BlockingQueue<PostPayloadTask> queue = new PriorityBlockingQueue<>();
    queue.add(newTask);
    queue.add(oldTask);

    // then
    assertThat(queue.poll()).isEqualTo(oldTask);
    assertThat(queue.poll()).isEqualTo(newTask);
  }

  @Test
  public void shouldSendPayload() throws JsonProcessingException {
    // given
    PostPayloadTask task = createTask(CLOCK, automaticPayloadRequest);

    // when
    task.run();

    // then
    verify(automaticPayloadRequest).createExecution(CLOCK);
    verify(payloadBuilder).build(START_DATE, END_DATE, null, null);
    verify(executionRepository, times(3)).saveAndFlush(execution);
    verify(objectMapper).writeValueAsString(anyString());
    verify(restTemplate).exchange(eq(TARGET_URL), eq(HttpMethod.POST),
        any(HttpEntity.class), eq(String.class));

    assertThat(execution.getRequestBody()).isEqualTo("{}");

    assertThat(getResponse(execution))
        .hasFieldOrPropertyWithValue("statusCode", 200)
        .hasFieldOrPropertyWithValue("body", "success");
  }

  @Test
  public void shouldHandle400And500ErrorsDuringSendingPayload() {
    // given
    RestClientResponseException exp = mock(RestClientResponseException.class);
    given(exp.getRawStatusCode()).willReturn(404);
    given(exp.getResponseBodyAsString()).willReturn("failure");

    given(restTemplate.exchange(eq(TARGET_URL), eq(HttpMethod.POST),
        any(HttpEntity.class), eq(String.class)))
        .willThrow(exp);

    PostPayloadTask task = createTask(CLOCK, automaticPayloadRequest);

    // when
    task.run();

    // then
    ExecutionResponseDto response = getResponse(execution);

    assertThat(response.getStatusCode()).isEqualTo(404);
    assertThat(response.getBody()).isEqualTo("failure");
  }

  @Test
  public void shouldHandleOtherErrorsDuringSendingPayload() {
    // given
    RuntimeException exp = mock(RuntimeException.class);
    given(exp.getMessage()).willReturn("runtimeException");

    given(restTemplate.exchange(eq(TARGET_URL), eq(HttpMethod.POST),
        any(HttpEntity.class), eq(String.class)))
        .willThrow(exp);

    PostPayloadTask task = createTask(CLOCK, automaticPayloadRequest);

    // when
    task.run();

    // then
    ExecutionResponseDto response = getResponse(execution);

    assertThat(response.getStatusCode()).isEqualTo(500);
    assertThat(response.getBody()).isEqualTo("runtimeException");
  }

  private ExecutionResponseDto getResponse(Execution execution) {
    return ExecutionDto.newInstance(execution).getResponse();
  }

  private PostPayloadTask createTask(Clock clock, PayloadRequest payloadRequest) {
    return new PostPayloadTask(programReferenceDataService, executionRepository, payloadBuilder,
        objectMapper, clock, restTemplate, payloadRequest);
  }
}

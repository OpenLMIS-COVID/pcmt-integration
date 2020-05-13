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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.openlmis.integration.pcmt.service.referencedata.orderable.DispensableDto;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings("PMD.TooManyMethods")
public class OrderableIntegrationSendTaskIntTest {

  private static final String OLMIS_URL = "http://localhost";

  private static final String API_PATH = "/api/orderables/";

  private static final String OLMIS_ORDERABLE_URL = OLMIS_URL + API_PATH;

  private static final UUID ADMIN_UUID = UUID.randomUUID();

  private static final Integration MANUAL_INT = new Integration("",
      "Manual integration");

  private static final ZonedDateTime BASE = ZonedDateTime.now();

  private static final Clock START = Clock.fixed(BASE.toInstant(), ZoneOffset.UTC);

  private static final UUID TOKEN = UUID.randomUUID();

  private static final UUID DEFAULT_ORDERABLE_UUID = UUID.randomUUID();

  private static final String NOT_FOUND_BODY = "{\n"
      + "  \"messageKey\" : \"referenceData.error.orderable.notFound\",\n"
      + "  \"message\" : \"Orderable not found\"\n"
      + "}";

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ExecutionRepository executionRepository;

  @Autowired
  private RestOperations restTemplate;

  @MockBean
  private AuthService authService;

  private MockRestServiceServer mockServer;

  @Before
  public void setUp() {
    mockServer = MockRestServiceServer.createServer((RestTemplate) restTemplate);
    given(authService.obtainAccessToken()).willReturn(TOKEN.toString());
  }

  @Test
  public void shouldCreateOrderable() throws JsonProcessingException {
    URI getUri = URI.create(OLMIS_ORDERABLE_URL + DEFAULT_ORDERABLE_UUID);
    mockGet404FromOlmisRequest(getUri);
    URI createUri = URI.create(OLMIS_ORDERABLE_URL);
    mockGet200FromOlmisRequest(createUri, createDefaultOrderable());

    OrderableIntegrationSendTask task = createTask(getMsgQueue());
    assertThat(executionRepository.count()).isEqualTo(0L);

    // Mocks IntegrationSendExecutor which is tested
    // at package org.openlmis.integration.pcmt.service.send.IntegrationSendExecutorIntTest
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(task);
    await().until(this::isExecuted);

    verify(authService, times(2)).obtainAccessToken();
    mockServer.verify();
    Execution execution = executionRepository.findAll().get(0);
    assertThat(execution.getRequestBody()).isNotNull();
    assertThat(execution.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(execution.getResponse().getBody()).contains(DEFAULT_ORDERABLE_UUID.toString());

    // simulate completing integration request
    executor.shutdownNow();
  }

  /**
   * Checks whether an integration of one orderable is completed.
   */
  private boolean isExecuted() {
    return executionRepository.count() > 0L
        && executionRepository.findAll().get(0).getResponse() != null;
  }

  private void mockGet404FromOlmisRequest(URI uri) {
    mockServer.expect(ExpectedCount.once(),
        requestTo(uri))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(NOT_FOUND_BODY));
  }

  private void mockGet200FromOlmisRequest(URI uri, OrderableDto dto)
      throws JsonProcessingException {
    mockServer.expect(ExpectedCount.manyTimes(),
        requestTo(uri))
        .andExpect(method(HttpMethod.PUT))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(objectMapper.writeValueAsString(dto)));
  }

  private OrderableIntegrationSendTask createTask(BlockingQueue<OrderableDto> queue) {
    return new OrderableIntegrationSendTask(
        queue, MANUAL_INT, ADMIN_UUID, OLMIS_URL, true,
        executionRepository, START, objectMapper, authService, restTemplate);
  }

  private BlockingQueue<OrderableDto> getMsgQueue() {
    BlockingQueue<OrderableDto> q = new LinkedBlockingDeque<>();
    q.add(createDefaultOrderable());
    return q;
  }

  private OrderableDto createDefaultOrderable() {
    OrderableDto dto = new OrderableDto();
    DispensableDto dispensableDto = new DispensableDto("Piece",
        null, null, null);
    dto.setId(DEFAULT_ORDERABLE_UUID);
    dto.setDispensable(dispensableDto);
    dto.setFullProductName("Respirator, Mask, N95/FFP2 Size Medium");
    dto.setProductCode("PPE100");
    dto.setNetContent(100000L);
    dto.setDescription("Respirator, Mask, N95/FFP2 Size Medium");
    dto.setPackRoundingThreshold(4L);
    dto.setRoundToZero(false);

    return dto;
  }

}

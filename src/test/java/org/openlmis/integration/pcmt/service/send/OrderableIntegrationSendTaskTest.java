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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.springframework.web.client.RestTemplate;

public class OrderableIntegrationSendTaskTest {

  private static final String OLMIS_ORDERABLE_URL = "http://localhost/api/orderables";

  private static final UUID ADMIN_UUID = UUID.randomUUID();

  private static final Integration MANUAL_INT = new Integration("",
      "Manual integration");

  private static final Integration SCHEDULED_INT = new Integration(
      "0 30 9 * * *", "Scheduled integration");

  private static final ZonedDateTime BASE = ZonedDateTime.now();
  private static final Clock START = Clock.fixed(BASE.toInstant(), ZoneOffset.UTC);
  private static final Clock START_MINUS_7_DAYS = Clock.fixed(
      BASE.minusDays(7).toInstant(), ZoneOffset.UTC);

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private ExecutionRepository executionRepository;

  @Test
  public void automaticTasksShouldBeBeforeManualTasks() {
    final BlockingQueue<OrderableDto> msgQueue = getEmptyMsgQueue();
    OrderableIntegrationSendTask automaticTask = createScheduledTask(msgQueue, START);
    OrderableIntegrationSendTask manualTask = createManualTask(msgQueue, START);

    BlockingQueue<IntegrationSendTask<?>> queue = new PriorityBlockingQueue<>();
    queue.add(manualTask);
    queue.add(automaticTask);

    assertThat(queue.poll()).isEqualTo(automaticTask);
    assertThat(queue.poll()).isEqualTo(manualTask);
  }

  @Test
  public void oldTasksShouldBeBeforeNewTasks() {
    final BlockingQueue<OrderableDto> msgQueue = getEmptyMsgQueue();
    OrderableIntegrationSendTask oldTask = createManualTask(msgQueue, START_MINUS_7_DAYS);
    OrderableIntegrationSendTask newTask = createManualTask(msgQueue, START);

    BlockingQueue<IntegrationSendTask<?>> queue = new PriorityBlockingQueue<>();
    queue.add(newTask);
    queue.add(oldTask);

    assertThat(queue.poll()).isEqualTo(oldTask);
    assertThat(queue.poll()).isEqualTo(newTask);
  }

  private OrderableIntegrationSendTask createManualTask(BlockingQueue<OrderableDto> queue,
      Clock start) {
    return createTask(true, ADMIN_UUID, queue, MANUAL_INT, start);
  }

  private OrderableIntegrationSendTask createScheduledTask(BlockingQueue<OrderableDto> queue,
      Clock start) {
    return createTask(false, null, queue, SCHEDULED_INT, start);
  }

  private OrderableIntegrationSendTask createTask(boolean manualExecution, UUID user,
      BlockingQueue<OrderableDto> queue, Integration integration, Clock start) {
    return new OrderableIntegrationSendTask(
        queue, integration, user, OLMIS_ORDERABLE_URL, manualExecution,
        executionRepository, start, objectMapper, authService, restTemplate);
  }

  private BlockingQueue<OrderableDto> getEmptyMsgQueue() {
    return new LinkedBlockingDeque<>();
  }
}

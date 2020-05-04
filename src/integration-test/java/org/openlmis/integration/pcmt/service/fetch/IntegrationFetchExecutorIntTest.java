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

package org.openlmis.integration.pcmt.service.fetch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.ZonedDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class IntegrationFetchExecutorIntTest {

  private static final ZonedDateTime baseTime = ZonedDateTime.now();

  @Autowired
  private IntegrationFetchExecutor executor;

  @Test
  public void shouldGetTasksInQueueWithoutRemovingThem() {
    // given
    FetchTestTask task1 = new FetchTestTask(baseTime.plusNanos(1L));
    FetchTestTask task2 = new FetchTestTask(baseTime.plusNanos(2L));
    FetchTestTask task3 = new FetchTestTask(baseTime.plusNanos(3L));

    // expect
    executor.execute(task1);
    executor.execute(task2);
    executor.execute(task3);

    // task1 is being executed
    // task2 and task3 should be in the queue
    assertThat(executor.getQueueItems()).hasSize(2);
    assertThat(task2.isExecuted()).isFalse();
    assertThat(task3.isExecuted()).isFalse();

    // task1 has been executed
    // task2 is being executed
    // task3 should be in the queue
    await().until(task1::isExecuted);
    assertThat(executor.getQueueItems()).hasSize(1);
    assertThat(task3.isExecuted()).isFalse();

    // task2 has been executed
    // task3 is being executed
    // queue should be empty
    await().until(task2::isExecuted);
    assertThat(executor.getQueueItems()).hasSize(0);
    assertThat(task1.isExecuted()).isTrue();
    assertThat(task2.isExecuted()).isTrue();

    // task3 has been executed
    // queue should still be empty
    await().until(task3::isExecuted);
    assertThat(executor.getQueueItems()).hasSize(0);
    assertThat(task3.isExecuted()).isTrue();
  }
}

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

package org.openlmis.integration.pcmt.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.pcmt.ToStringTestUtils;
import org.openlmis.integration.pcmt.testbuilder.ExecutionResponseDataBuilder;
import org.openlmis.integration.pcmt.testbuilder.IntegrationDataBuilder;

public class ExecutionTest {

  private static final Integration INTEGRATION = new IntegrationDataBuilder().build();
  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private static final ZonedDateTime START_DATE = ZonedDateTime.now(CLOCK);
  private static final ZonedDateTime END_DATE = ZonedDateTime.now(CLOCK);
  private static final ExecutionResponse RESPONSE = new ExecutionResponseDataBuilder()
      .withStatusCode(200)
      .build();
  private static final UUID USER_ID = UUID.randomUUID();

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Execution.class)
        .withRedefinedSuperclass()
        .withPrefabValues(ExecutionResponse.class,
            new ExecutionResponse(ZonedDateTime.now(), 200, "ok"),
            new ExecutionResponse(ZonedDateTime.now(), 404, "notFound"))
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Execution execution = new Execution();
    ToStringTestUtils.verify(Execution.class, execution, "EMPTY_JSON");
  }

  @Test
  public void shouldCreateInstanceForAutomaticExecution() {
    // when
    Execution execution = Execution
        .forAutomaticExecution(INTEGRATION, CLOCK);

    // then
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    assertThat(exporter.getId()).isEqualTo(execution.getId());
    assertThat(exporter.isManualExecution()).isFalse();
    assertThat(exporter.getStartDate()).isEqualTo(START_DATE);
    assertThat(exporter.getEndDate()).isNull();
    assertThat(exporter.getResponse()).isNull();
    assertThat(exporter.getUserId()).isNull();
  }

  @Test
  public void shouldCreateInstanceForManualExecution() {
    // when
    Execution execution = Execution.forManualExecution(INTEGRATION, USER_ID, CLOCK);

    // then
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    assertThat(exporter.getId()).isEqualTo(execution.getId());
    assertThat(exporter.isManualExecution()).isTrue();
    assertThat(exporter.getStatus()).isEqualTo(ExecutionStatus.STARTED);
    assertThat(exporter.getStartDate()).isEqualTo(START_DATE);
    assertThat(exporter.getEndDate()).isNull();
    assertThat(exporter.getResponse()).isNull();
    assertThat(exporter.getUserId()).isEqualTo(USER_ID);
  }

  @Test
  public void shouldSetRequestBody() {
    // given
    Execution execution = Execution
        .forAutomaticExecution(INTEGRATION, CLOCK);
    execution.setId(UUID.randomUUID());
    String requestBody = "{}";

    // when
    execution.setRequestBody(requestBody);

    // then
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    assertThat(execution.getRequestBody()).isEqualTo(requestBody);
    assertThat(exporter.getStatus()).isEqualTo(ExecutionStatus.PENDING);
  }

  @Test
  public void shouldMarkAsDone() {
    // given
    Execution execution = Execution
        .forAutomaticExecution(INTEGRATION, CLOCK);
    execution.setId(UUID.randomUUID());

    TestExecutionResponse expectedResponse = new TestExecutionResponse();
    RESPONSE.export(expectedResponse);

    // when
    execution.markAsDone(RESPONSE, CLOCK);

    // then
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    assertThat(exporter.getEndDate()).isEqualTo(ZonedDateTime.now(CLOCK));
    assertThat(exporter.getResponse()).isEqualTo(expectedResponse);
    assertThat(exporter.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
  }

  @Test
  public void shouldExportWithoutResponse() {
    // given
    Execution execution = Execution
        .forAutomaticExecution(INTEGRATION, CLOCK);
    execution.setId(UUID.randomUUID());

    // when
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    // then
    assertThat(exporter.getId()).isEqualTo(execution.getId());
    assertThat(exporter.isManualExecution()).isFalse();
    assertThat(exporter.getStartDate()).isEqualTo(START_DATE);
    assertThat(exporter.getEndDate()).isNull();
    assertThat(exporter.getResponse()).isNull();
    assertThat(exporter.getUserId()).isNull();
  }

  @Test
  public void shouldExportWithResponse() {
    Execution execution = Execution
        .forAutomaticExecution(INTEGRATION, CLOCK);
    execution.setId(UUID.randomUUID());
    execution.markAsDone(RESPONSE, CLOCK);

    TestExecutionResponse expectedResponse = new TestExecutionResponse();
    RESPONSE.export(expectedResponse);

    // when
    TestExecution exporter = new TestExecution();
    execution.export(exporter);

    // then
    assertThat(exporter.getId()).isEqualTo(execution.getId());
    assertThat(exporter.isManualExecution()).isFalse();
    assertThat(exporter.getStartDate()).isEqualTo(START_DATE);
    assertThat(exporter.getEndDate()).isEqualTo(END_DATE);
    assertThat(exporter.getResponse()).isEqualTo(expectedResponse);
    assertThat(exporter.getUserId()).isNull();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  private static final class TestExecution implements Execution.Exporter {

    private UUID id;
    private boolean manualExecution;
    private UUID programId;
    private ExecutionStatus status;
    private String description;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private UUID userId;
    private TestExecutionResponse response;

    @Override
    public void setResponse(ExecutionResponse response) {
      this.response = new TestExecutionResponse();
      response.export(this.response);
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  private static final class TestExecutionResponse implements ExecutionResponse.Exporter {

    private UUID id;
    private ZonedDateTime responseDate;
    private int statusCode;
    private String body;
  }

}

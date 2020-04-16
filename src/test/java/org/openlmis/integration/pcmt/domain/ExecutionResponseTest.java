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

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.pcmt.ToStringTestUtils;

public class ExecutionResponseTest {

  @Test
  public void equalsContract() {
    Execution left = new Execution();
    left.setId(UUID.randomUUID());

    Execution right = new Execution();
    right.setId(UUID.randomUUID());

    EqualsVerifier
        .forClass(ExecutionResponse.class)
        .withPrefabValues(Execution.class, left, right)
        .withIgnoredFields("execution")
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ExecutionResponse response = new ExecutionResponse();
    ToStringTestUtils.verify(ExecutionResponse.class, response, "execution");
  }

  @Test
  public void shouldExportData() {
    // given
    ZonedDateTime responseDate = ZonedDateTime.now();
    int statusCode = 200;
    String body = "body";

    ExecutionResponse response = new ExecutionResponse(responseDate, statusCode, body);

    // when
    TestExecutionResponse exporter = new TestExecutionResponse();
    response.export(exporter);

    // then
    assertThat(exporter)
        .hasFieldOrPropertyWithValue("responseDate", responseDate)
        .hasFieldOrPropertyWithValue("statusCode", statusCode)
        .hasFieldOrPropertyWithValue("body", body);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  private static final class TestExecutionResponse implements ExecutionResponse.Exporter {

    private UUID id;
    private ZonedDateTime responseDate;
    private int statusCode;
    private String body;
  }

}

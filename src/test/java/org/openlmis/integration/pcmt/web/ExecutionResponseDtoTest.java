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

package org.openlmis.integration.pcmt.web;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;
import org.openlmis.integration.pcmt.ToStringTestUtils;
import org.openlmis.integration.pcmt.testbuilder.ExecutionResponseDataBuilder;

public class ExecutionResponseDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ExecutionResponseDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ExecutionResponseDto dto = ExecutionResponseDto.newInstance(
        new ExecutionResponseDataBuilder().build()
    );

    ToStringTestUtils.verify(ExecutionResponseDto.class, dto);
  }
}

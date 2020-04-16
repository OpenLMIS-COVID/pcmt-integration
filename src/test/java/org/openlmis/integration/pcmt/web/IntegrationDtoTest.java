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

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.integration.pcmt.ConfigurationDataBuilder;
import org.openlmis.integration.pcmt.IntegrationDataBuilder;
import org.openlmis.integration.pcmt.ToStringTestUtils;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.exception.ValidationMessageException;
import org.openlmis.integration.pcmt.i18n.MessageKeys;


public class IntegrationDtoTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void equalsContract() {

    EqualsVerifier
        .forClass(IntegrationDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    IntegrationDto dto = IntegrationDto.newInstance(
        new IntegrationDataBuilder().buildAsNew()
    );

    ToStringTestUtils.verify(IntegrationDto.class, dto);
  }

  @Test
  public void shouldGetConfigurationId() {
    IntegrationDto dto = new IntegrationDto();

    Configuration configuration = new ConfigurationDataBuilder().build();
    Integration integration = new IntegrationDataBuilder()
        .withConfiguration(configuration)
        .build();
    integration.export(dto);

    assertThat(dto.getConfigurationId()).isEqualTo(configuration.getId());
  }

  @Test
  public void shouldThrowExceptionIfConfigurationDoesNotExist() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(MessageKeys.ERROR_INTEGRATION_CONFIGURATION_REQUIRED);

    IntegrationDto dto = new IntegrationDto();
    dto.setConfiguration((ConfigurationDto) null);

    dto.getConfigurationId();
  }
}

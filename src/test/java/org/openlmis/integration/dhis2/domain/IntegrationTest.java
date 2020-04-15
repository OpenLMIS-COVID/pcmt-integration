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

package org.openlmis.integration.dhis2.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.dhis2.ConfigurationDataBuilder;
import org.openlmis.integration.dhis2.ToStringTestUtils;

public class IntegrationTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Integration.class)
        .withPrefabValues(Configuration.class,
            new Configuration(),
            new ConfigurationDataBuilder().build())
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Integration integration = new Integration();
    ToStringTestUtils.verify(Integration.class, integration);
  }

  @Test
  public void shouldUpdateFromImporter() {
    // given
    TestIntegration data = new TestIntegration(UUID.randomUUID(),
        UUID.randomUUID(), "cron", "description", null);
    Integration integration = new Integration();
    integration.setId(UUID.randomUUID());

    // when
    integration.updateFrom(data);

    // then
    assertThat(integration)
        .hasFieldOrPropertyWithValue("id", integration.getId())
        .hasFieldOrPropertyWithValue("programId", data.getProgramId())
        .hasFieldOrPropertyWithValue("description", data.getDescription())
        .hasFieldOrPropertyWithValue("cronExpression", data.getCronExpression());
  }

  @Test
  public void shouldExportData() {
    // given
    Configuration configuration = new Configuration();
    TestIntegration data = new TestIntegration(UUID.randomUUID(),
        UUID.randomUUID(), "cron", "description", configuration);

    Integration integration = new Integration();
    integration.setId(UUID.randomUUID());
    integration.updateFrom(data);
    integration.setConfiguration(configuration);

    // when
    TestIntegration exporter = new TestIntegration();
    integration.export(exporter);

    // then
    assertThat(exporter).isEqualToIgnoringGivenFields(data, "id");
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  private static final class TestIntegration implements Integration.Importer, Integration.Exporter {
    private UUID id;
    private UUID programId;
    private String cronExpression;
    private String description;
    private Configuration configuration;
  }

}

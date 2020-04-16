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

package org.openlmis.integration.pcmt;

import java.util.UUID;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.Integration;

public class IntegrationDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private String cronExpression = "0/30 * * * * *";
  private String description = "test-description";
  private Configuration configuration = new ConfigurationDataBuilder().build();

  public IntegrationDataBuilder withProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public IntegrationDataBuilder withCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
    return this;
  }

  public IntegrationDataBuilder withConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  public Integration buildAsNew() {
    return new Integration(programId, cronExpression, description, configuration);
  }

  /**
   * Creates new instance with id field set.
   */
  public Integration build() {
    Integration integration = buildAsNew();
    integration.setId(id);

    return integration;
  }
}

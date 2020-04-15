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

package org.openlmis.integration.dhis2;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.openlmis.integration.dhis2.domain.Execution;
import org.openlmis.integration.dhis2.domain.ExecutionResponse;
import org.openlmis.integration.dhis2.domain.Integration;

public class ExecutionDataBuilder {

  private UUID id = UUID.randomUUID();
  private Integration integration = new IntegrationDataBuilder().build();
  private UUID facilityId = UUID.randomUUID();
  private UUID processingPeriodId = UUID.randomUUID();
  private String description = "test-description";
  private Clock startDate = Clock.systemUTC();
  private Clock endDate = Clock.systemUTC();
  private UUID userId = UUID.randomUUID();
  private ExecutionResponse response = new ExecutionResponseDataBuilder().build();

  public ExecutionDataBuilder withFacilityId(UUID facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  public ExecutionDataBuilder withProcessingPeriodId(UUID processingPeriodId) {
    this.processingPeriodId = processingPeriodId;
    return this;
  }

  public ExecutionDataBuilder withStartDate(Clock startDate) {
    this.startDate = startDate;
    return this;
  }

  public ExecutionDataBuilder withEndDate(Clock endDate) {
    this.endDate = endDate;
    return this;
  }

  public ExecutionDataBuilder withoutResponse() {
    this.response = null;
    return this;
  }

  /**
   * Builds new instance of Execution (with id field) as Automatic execution.
   */
  public Execution buildAsAutomatic() {
    Execution execution = buildAsNewAutomatic();
    execution.setId(id);
    return execution;
  }

  /**
   * Builds new instance of Execution (with id field) as Manual execution.
   */
  public Execution buildAsManual() {
    Execution execution = buildAsNewManual();
    execution.setId(id);
    return execution;
  }

  /**
   * Builds new instance of Execution as a new object (without id field) as Automatic execution.
   */

  public Execution buildAsNewAutomatic() {
    Execution execution = Execution
        .forAutomaticExecution(integration, processingPeriodId, startDate);
    Optional.ofNullable(response).ifPresent(item -> execution.markAsDone(item, endDate));

    return execution;
  }

  /**
   * Builds new instance of Execution as a new object (without id field) as Manual execution.
   */

  public Execution buildAsNewManual() {
    Execution execution = Execution
        .forManualExecution(integration, facilityId, processingPeriodId, description,
            userId, startDate);
    Optional.ofNullable(response).ifPresent(item -> execution.markAsDone(item, endDate));

    return execution;
  }
}

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

package org.openlmis.integration.pcmt.service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.ConfigurationAuthenticationDetails;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PayloadRequest {

  private final Integration integration;

  @Getter
  private final UUID facilityId;

  @Getter
  private final ProcessingPeriodDto period;

  @Getter
  private String description;

  @Getter
  private final boolean manualExecution;

  @Getter
  private final UUID userId;

  public UUID getProgramId() {
    return integration.getProgramId();
  }

  Execution createExecution(Clock clock) {
    if (manualExecution) {
      return Execution.forManualExecution(integration, facilityId, period.getId(),
          description, userId, clock);
    } else {
      return Execution.forAutomaticExecution(integration, period.getId(), clock);
    }
  }

  public String getTargetUrl() {
    return integration.getTargetUrl();
  }

  String getAuthorizationHeader() {
    return Optional
        .ofNullable(integration)
        .map(Integration::getConfiguration)
        .map(Configuration::getAuthenticationDetails)
        .map(ConfigurationAuthenticationDetails::asAuthorizationHeader)
        .orElse(null);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
        .append("integrationId", integration.getId())
        .append("programId", getProgramId())
        .append("targetUrl", getTargetUrl())
        .append("facilityId", facilityId)
        .append("periodName", period.getName())
        .append("description", description)
        .append("manualExecution", manualExecution)
        .append("userId", userId)
        .toString();
  }
}

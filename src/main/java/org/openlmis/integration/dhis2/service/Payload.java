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

package org.openlmis.integration.dhis2.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Model of Payload DTO. Instance of this object will be send to DHIS2
 */

@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
final class Payload {
  private static final DateTimeFormatter DESCRIPTION_FORMATTER = DateTimeFormatter
      .ofPattern("MMMM yyyy");
  private static final DateTimeFormatter REPORTING_PERIOD_FORMATTER = DateTimeFormatter
      .ofPattern("yyyyMM");

  private final String description;
  private final Set<PayloadFacility> facilities;

  @JsonProperty("reporting-period")
  private final String reportingPeriod;

  Payload(Set<PayloadFacility> facilities, LocalDate reportingPeriod) {
    this.facilities = Collections.unmodifiableSet(facilities);
    this.description = String
        .format("Stock indicators for %s period", reportingPeriod.format(DESCRIPTION_FORMATTER));
    this.reportingPeriod = reportingPeriod.format(REPORTING_PERIOD_FORMATTER);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
        .append("description", description)
        .append("reportingPeriod", reportingPeriod)
        .toString();
  }
}


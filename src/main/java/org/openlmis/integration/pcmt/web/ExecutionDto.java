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

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jackson.annotate.JsonSetter;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.ExecutionStatus;


/**
 * Model of ExecutionDto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ExecutionDto extends BaseDto implements Execution.Exporter {

  private boolean manualExecution;
  private UUID programId;
  private UUID facilityId;
  private UUID processingPeriodId;
  private ExecutionStatus status;
  private String description;
  private String targetUrl;
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;
  private UUID userId;
  private ExecutionResponseDto response;

  /**
   * Creates new instance based on domain object.
   */

  public static ExecutionDto newInstance(Execution execution) {
    ExecutionDto dto = new ExecutionDto();
    execution.export(dto);
    return dto;
  }

  @Override
  public void setResponse(ExecutionResponse response) {
    this.response = new ExecutionResponseDto();
    response.export(this.response);
  }

  @JsonSetter("ExecutionResponseDto")
  public void setResponse(ExecutionResponseDto executionResponseDto) {
    this.response = executionResponseDto;
  }

}

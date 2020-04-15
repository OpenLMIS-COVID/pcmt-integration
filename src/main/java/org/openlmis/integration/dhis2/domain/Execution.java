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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "executions")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Execution extends BaseEntity {

  private static final String EMPTY_JSON = "{}";

  @Column(nullable = false)
  private boolean manualExecution;

  @Type(type = UUID_TYPE)
  private UUID programId;

  @Type(type = UUID_TYPE)
  private UUID facilityId;

  @Type(type = UUID_TYPE)
  @Column(nullable = false)
  private UUID processingPeriodId;

  @Enumerated(EnumType.STRING)
  private ExecutionStatus status;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String description;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String targetUrl;

  @Column(nullable = false, columnDefinition = TIMESTAMP_COLUMN_DEFINITION)
  private ZonedDateTime startDate;

  @Column(columnDefinition = TIMESTAMP_COLUMN_DEFINITION)
  private ZonedDateTime endDate;

  @Getter
  @Basic(fetch = FetchType.LAZY)
  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String requestBody;

  @Type(type = UUID_TYPE)
  @Column
  private UUID userId;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "execution",
      orphanRemoval = true, fetch = FetchType.EAGER)
  private ExecutionResponse response;

  /**
   * Creates a new automatic execution.
   */
  public static Execution forAutomaticExecution(Integration integration, UUID processingPeriodId,
      Clock clock) {
    return new Execution(false, integration.getProgramId(), null, processingPeriodId,
        ExecutionStatus.STARTED, integration.getDescription(), integration.getTargetUrl(),
        ZonedDateTime.now(clock), null, EMPTY_JSON, null, null);
  }

  /**
   * Creates a new manual execution.
   */

  public static Execution forManualExecution(Integration integration, UUID facilityId,
      UUID processingPeriodId, String description, UUID userId, Clock clock) {
    return new Execution(true, integration.getProgramId(), facilityId, processingPeriodId,
        ExecutionStatus.STARTED, description, integration.getTargetUrl(),
        ZonedDateTime.now(clock), null, EMPTY_JSON, userId, null);
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
    this.status = ExecutionStatus.PENDING;
  }

  /**
   * mark this execution as done.
   */
  public void markAsDone(ExecutionResponse response, Clock clock) {
    this.response = response;
    this.response.setExecution(this);

    this.endDate = ZonedDateTime.now(clock);
    this.status = response.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.ERROR;
  }

  /**
   * Export the current object state.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setManualExecution(manualExecution);
    exporter.setProgramId(programId);
    exporter.setFacilityId(facilityId);
    exporter.setProcessingPeriodId(processingPeriodId);
    exporter.setStatus(status);
    exporter.setDescription(description);
    exporter.setTargetUrl(targetUrl);
    exporter.setStartDate(startDate);

    if (null != endDate) {
      exporter.setEndDate(endDate);
    }

    if (null != response) {
      exporter.setResponse(response);
    }

    if (null != userId) {
      exporter.setUserId(userId);
    }
  }

  public interface Exporter extends BaseExporter {

    void setManualExecution(boolean manualExecution);

    void setProgramId(UUID programId);

    void setFacilityId(UUID facilityId);

    void setProcessingPeriodId(UUID processingPeriodId);

    void setStatus(ExecutionStatus status);

    void setDescription(String description);

    void setTargetUrl(String targetUrl);

    void setStartDate(ZonedDateTime startDate);

    void setEndDate(ZonedDateTime endDate);

    void setResponse(ExecutionResponse response);

    void setUserId(UUID userId);

  }
}

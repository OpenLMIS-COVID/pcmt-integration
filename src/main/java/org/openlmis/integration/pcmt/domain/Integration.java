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

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Table(name = "integrations")
@TypeName("Integration")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Integration extends BaseEntity {

  @Getter
  @Type(type = UUID_TYPE)
  private UUID programId;

  @Getter
  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String cronExpression;

  @Getter
  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String description;

  @Getter
  @Setter
  @ManyToOne
  @JoinColumn(name = "configurationId", nullable = false)
  private Configuration configuration;

  public String getTargetUrl() {
    return configuration.getTargetUrl();
  }

  /**
   * Update this from another.
   */
  public void updateFrom(Importer importer) {
    this.programId = importer.getProgramId();
    this.cronExpression = importer.getCronExpression();
    this.description = importer.getDescription();
  }

  /**
   * Export the current object state.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setProgramId(programId);
    exporter.setCronExpression(cronExpression);
    exporter.setDescription(description);
    exporter.setConfiguration(configuration);
  }

  public interface Importer extends BaseImporter {

    UUID getProgramId();

    String getCronExpression();

    String getDescription();

  }

  public interface Exporter extends BaseExporter {

    void setProgramId(UUID programId);

    void setCronExpression(String cronExpression);

    void setDescription(String description);

    void setConfiguration(Configuration configuration);
  }

}

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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Table(name = "configurations")
@TypeName("Configuration")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Configuration extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String name;

  @Getter(AccessLevel.PACKAGE)
  @Column(nullable = false, unique = true, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String targetUrl;

  @Getter
  @OneToOne(cascade = CascadeType.ALL, mappedBy = "configuration",
      orphanRemoval = true, fetch = FetchType.EAGER)
  private ConfigurationAuthenticationDetails authenticationDetails;

  /**
   * Creates new instance with the passed data.
   */
  public Configuration(String name, String targetUrl,
      ConfigurationAuthenticationDetails authenticationDetails) {
    this.name = name;
    this.targetUrl = targetUrl;
    this.authenticationDetails = authenticationDetails;

    if (null != this.authenticationDetails) {
      this.authenticationDetails.setConfiguration(this);
    }
  }

  /**
   * Update this from another.
   */
  public void updateFrom(Importer importer) {
    this.name = importer.getName();
    this.targetUrl = importer.getTargetUrl();

    if (null != importer.getAuthenticationDetails()) {
      if (null == authenticationDetails) {
        this.authenticationDetails = new ConfigurationAuthenticationDetails();
      }

      this.authenticationDetails.updateFrom(importer.getAuthenticationDetails());
      this.authenticationDetails.setConfiguration(this);
    } else if (null != this.authenticationDetails) {
      this.authenticationDetails.setConfiguration(null);
      this.authenticationDetails = null;
    }
  }

  /**
   * Export the current object state.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setTargetUrl(targetUrl);

    if (null != authenticationDetails) {
      exporter.setAuthenticationDetails(authenticationDetails);
    }
  }

  public interface Importer extends BaseImporter {

    String getName();

    String getTargetUrl();

    ConfigurationAuthenticationDetails.Importer getAuthenticationDetails();

  }

  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setTargetUrl(String targetUrl);

    void setAuthenticationDetails(ConfigurationAuthenticationDetails authenticationDetails);

  }

}

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

package org.openlmis.integration.dhis2.web;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationDetails;
import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationDetails.Exporter;
import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationDetails.Importer;
import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationType;



/**
 * Model of ConfigurationAuthenticationDetailsDto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(callSuper = true)
public final class ConfigurationAuthenticationDetailsDto implements Importer,
    Exporter {
  private ConfigurationAuthenticationType type;
  private String username;
  private String password;
  private String token;

  public ConfigurationAuthenticationDetailsDto(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public ConfigurationAuthenticationDetailsDto(String token) {
    this.token = token;
  }

  /**
   * Creates new instance based on domain object.
   */
  public static ConfigurationAuthenticationDetailsDto newInstance(
      ConfigurationAuthenticationDetails configurationAuthenticationDetails) {
    ConfigurationAuthenticationDetailsDto dto = new ConfigurationAuthenticationDetailsDto();
    configurationAuthenticationDetails.export(dto);
    return dto;
  }
}
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

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Table(name = "configuration_authentication_details")
@TypeName("ConfigurationAuthenticationDetails")
@NoArgsConstructor
@EqualsAndHashCode(exclude = "configuration")
@ToString(exclude = "configuration")
@SuppressWarnings("PMD.UnusedPrivateField")
public final class ConfigurationAuthenticationDetails {

  private static final String AUTHORIZATION_HEADER_FORMAT = "%s %s";

  @Id
  private UUID id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ConfigurationAuthenticationType type;

  @Column(columnDefinition = BaseEntity.TEXT_COLUMN_DEFINITION)
  private String username;

  @Column(columnDefinition = BaseEntity.TEXT_COLUMN_DEFINITION)
  private String password;

  @Column(columnDefinition = BaseEntity.TEXT_COLUMN_DEFINITION)
  private String token;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  private Configuration configuration;

  public ConfigurationAuthenticationDetails(String token) {
    this(ConfigurationAuthenticationType.BEARER, null, null, token);
  }

  public ConfigurationAuthenticationDetails(String username, String password) {
    this(ConfigurationAuthenticationType.BASIC, username, password, null);
  }

  /**
   * Creates new instance with passed data.
   */
  private ConfigurationAuthenticationDetails(ConfigurationAuthenticationType type,
      String username, String password, String token) {
    this.type = type;
    this.username = username;
    this.password = password;
    this.token = token;
  }

  /**
   * Convert the given {@link ConfigurationAuthenticationDetails} into {@link String} that can be
   * used as Authorization Header.
   */
  public String asAuthorizationHeader() {
    String value;

    if (type == ConfigurationAuthenticationType.BASIC) {
      value = Base64.encodeBase64String((username + ":" + password).getBytes());
    } else if (type == ConfigurationAuthenticationType.BEARER) {
      value = token;
    } else {
      throw new IllegalStateException("Unknown type: " + type);
    }

    return String.format(AUTHORIZATION_HEADER_FORMAT, type.name(), value);
  }

  void setConfiguration(Configuration configuration) {
    if (null != configuration) {
      this.id = configuration.getId();
      this.configuration = configuration;
    } else {
      this.id = null;
      this.configuration = null;
    }
  }

  /**
   * Update this from another.
   */
  void updateFrom(Importer importer) {
    // we don't have to verify if properties are related to the selected type
    // the database layer do this for us
    this.type = importer.getType();
    this.username = importer.getUsername();
    this.password = importer.getPassword();
    this.token = importer.getToken();
  }

  /**
   * Export the current object state.
   */
  public void export(Exporter exporter) {
    exporter.setType(type);
    exporter.setUsername(username);
    exporter.setPassword(password);
    exporter.setToken(token);
  }

  public interface Importer {

    ConfigurationAuthenticationType getType();

    String getUsername();

    String getPassword();

    String getToken();

  }

  public interface Exporter {

    void setType(ConfigurationAuthenticationType type);

    void setUsername(String username);

    void setPassword(String password);

    void setToken(String token);

  }

}

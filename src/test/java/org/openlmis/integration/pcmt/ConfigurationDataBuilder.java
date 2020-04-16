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
import java.util.concurrent.atomic.AtomicInteger;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.ConfigurationAuthenticationDetails;

public class ConfigurationDataBuilder {

  private static AtomicInteger instanceNumber = new AtomicInteger(0);

  private UUID id = UUID.randomUUID();
  private String name = "test-configuration-" + instanceNumber.incrementAndGet();
  private String targetUrl = "http://test.configuration";
  private ConfigurationAuthenticationDetails authenticationDetails;

  public ConfigurationDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ConfigurationDataBuilder withTargetUrl(String targetUrl) {
    this.targetUrl = targetUrl;
    return this;
  }

  public ConfigurationDataBuilder withCredentials(String token) {
    this.authenticationDetails = new ConfigurationAuthenticationDetails(token);
    return this;
  }

  public ConfigurationDataBuilder withCredentials(String username, String password) {
    this.authenticationDetails = new ConfigurationAuthenticationDetails(username, password);
    return this;
  }

  public ConfigurationDataBuilder withoutCredentials() {
    this.authenticationDetails = null;
    return this;
  }

  public Configuration buildAsNew() {
    return new Configuration(name, targetUrl, authenticationDetails);
  }

  /**
   * Creates new instance with id field set.
   */
  public Configuration build() {
    Configuration configuration = buildAsNew();
    configuration.setId(id);

    return configuration;
  }
}

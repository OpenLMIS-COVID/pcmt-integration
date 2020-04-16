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

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.integration.pcmt.domain.ConfigurationAuthenticationType.BEARER;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.integration.pcmt.ToStringTestUtils;

public class ConfigurationTest {

  private static final String NAME = "test";
  private static final String TARGET_URL = "http://test";

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Configuration.class)
        .withPrefabValues(ConfigurationAuthenticationDetails.class,
            new ConfigurationAuthenticationDetails("user", "pass"),
            new ConfigurationAuthenticationDetails("token"))
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Configuration configuration = new Configuration();
    ToStringTestUtils.verify(Configuration.class, configuration);
  }

  @Test
  public void shouldUpdateFromImporterWithoutCredentials() {
    // given
    TestConfiguration data = new TestConfiguration(UUID.randomUUID(), NAME, TARGET_URL, null);
    Configuration configuration = new Configuration();
    configuration.setId(UUID.randomUUID());

    // when
    configuration.updateFrom(data);

    // then
    assertThat(configuration)
        .hasFieldOrPropertyWithValue("id", configuration.getId())
        .hasFieldOrPropertyWithValue("name", data.getName())
        .hasFieldOrPropertyWithValue("targetUrl", data.getTargetUrl())
        .hasFieldOrPropertyWithValue("authenticationDetails", null);
  }

  @Test
  public void shouldUpdateFromImporterWithCredentials() {
    // given
    TestConfigurationAuthenticationDetails credentials =
        new TestConfigurationAuthenticationDetails(UUID.randomUUID(), BEARER, null,
            null, UUID.randomUUID().toString());
    TestConfiguration data = new TestConfiguration(UUID.randomUUID(), NAME,
        TARGET_URL, credentials);
    Configuration configuration = new Configuration();
    configuration.setId(UUID.randomUUID());

    // when
    configuration.updateFrom(data);

    // then
    assertThat(configuration)
        .hasFieldOrPropertyWithValue("id", configuration.getId())
        .hasFieldOrPropertyWithValue("name", data.getName())
        .hasFieldOrPropertyWithValue("targetUrl", data.getTargetUrl())
        .hasFieldOrPropertyWithValue("authenticationDetails.type", credentials.getType())
        .hasFieldOrPropertyWithValue("authenticationDetails.token", credentials.getToken());
  }

  @Test
  public void shouldExportWithoutCredentials() {
    // given
    TestConfiguration data = new TestConfiguration(UUID.randomUUID(), NAME,
        TARGET_URL, null);
    Configuration configuration = new Configuration();
    configuration.setId(UUID.randomUUID());
    configuration.updateFrom(data);

    // when
    TestConfiguration exporter = new TestConfiguration();
    configuration.export(exporter);

    // then
    assertThat(exporter).isEqualToIgnoringGivenFields(data, "id");
  }

  @Test
  public void shouldExportWithCredentials() {
    // given
    TestConfigurationAuthenticationDetails credentials =
        new TestConfigurationAuthenticationDetails(UUID.randomUUID(), BEARER, null,
            null, UUID.randomUUID().toString());
    TestConfiguration data = new TestConfiguration(UUID.randomUUID(), NAME,
        TARGET_URL, credentials);
    Configuration configuration = new Configuration();
    configuration.setId(UUID.randomUUID());
    configuration.updateFrom(data);

    // when
    TestConfiguration exporter = new TestConfiguration();
    configuration.export(exporter);

    // then
    assertThat(exporter).isEqualToIgnoringGivenFields(data, "id", "authenticationDetails");
    assertThat(exporter.getAuthenticationDetails())
        .isEqualToIgnoringGivenFields(data.getAuthenticationDetails(), "id");
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  private static final class TestConfiguration
      implements Configuration.Importer, Configuration.Exporter {

    private UUID id;
    private String name;
    private String targetUrl;
    private TestConfigurationAuthenticationDetails authenticationDetails;


    @Override
    public void setAuthenticationDetails(ConfigurationAuthenticationDetails authenticationDetails) {
      this.authenticationDetails = new TestConfigurationAuthenticationDetails();
      authenticationDetails.export(this.authenticationDetails);
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  private static final class TestConfigurationAuthenticationDetails
      implements ConfigurationAuthenticationDetails.Importer,
      ConfigurationAuthenticationDetails.Exporter {

    private UUID id;
    private ConfigurationAuthenticationType type;
    private String username;
    private String password;
    private String token;
  }

}

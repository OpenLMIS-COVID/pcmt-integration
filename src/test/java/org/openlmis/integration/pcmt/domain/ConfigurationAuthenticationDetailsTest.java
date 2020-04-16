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
import org.openlmis.integration.pcmt.ConfigurationDataBuilder;
import org.openlmis.integration.pcmt.ToStringTestUtils;

public class ConfigurationAuthenticationDetailsTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ConfigurationAuthenticationDetails.class)
        .withPrefabValues(Configuration.class,
            new Configuration(),
            new ConfigurationDataBuilder().build())
        .withIgnoredFields("configuration")
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails();
    ToStringTestUtils.verify(
        ConfigurationAuthenticationDetails.class,
        details,
        "configuration", "AUTHORIZATION_HEADER_FORMAT");
  }

  @Test
  public void shouldUpdateFromImporter() {
    // given
    TestConfigurationAuthenticationDetails data =
        new TestConfigurationAuthenticationDetails(UUID.randomUUID(), BEARER,
            null, null, UUID.randomUUID().toString());
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails();

    // when
    details.updateFrom(data);

    // then
    assertThat(details)
        .hasFieldOrPropertyWithValue("type", data.getType())
        .hasFieldOrPropertyWithValue("username", data.getUsername())
        .hasFieldOrPropertyWithValue("password", data.getPassword())
        .hasFieldOrPropertyWithValue("token", data.getToken());
  }

  @Test
  public void shouldExportData() {
    // given
    TestConfigurationAuthenticationDetails data =
        new TestConfigurationAuthenticationDetails(UUID.randomUUID(), BEARER,
            null, null, UUID.randomUUID().toString());
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails();
    details.updateFrom(data);

    // when
    TestConfigurationAuthenticationDetails exporter = new TestConfigurationAuthenticationDetails();
    details.export(exporter);

    // then
    assertThat(exporter).isEqualToIgnoringGivenFields(data, "id");
  }

  @Test
  public void shouldConvertToAuthorizationHeaderForBasic() {
    // given
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails(
        "user", "pass");

    // when
    String header = details.asAuthorizationHeader();

    // then
    assertThat(header).isEqualTo("BASIC dXNlcjpwYXNz");
  }

  @Test
  public void shouldConvertToAuthorizationHeaderForBearer() {
    // given
    String token = "442f8efd-9121-404a-8aa6-bdca6ec75597";
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails(token);

    // when
    String header = details.asAuthorizationHeader();

    // then
    assertThat(header).isEqualTo("BEARER 442f8efd-9121-404a-8aa6-bdca6ec75597");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotConvertToAuthorizationHeaderForUnknownType() {
    // given
    ConfigurationAuthenticationDetails details = new ConfigurationAuthenticationDetails();

    // when
    details.asAuthorizationHeader();
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

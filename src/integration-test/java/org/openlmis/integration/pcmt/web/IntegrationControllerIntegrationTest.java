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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.integration.pcmt.ConfigurationDataBuilder;
import org.openlmis.integration.pcmt.IntegrationDataBuilder;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.i18n.MessageKeys;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
@Ignore
public class IntegrationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = IntegrationController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + IntegrationController.ID_URL;

  private Configuration configuration = new ConfigurationDataBuilder()
      .build();
  private Integration integration = new IntegrationDataBuilder()
      .withConfiguration(configuration)
      .build();

  private IntegrationDto integrationDto = new IntegrationDto();

  @Before
  public void setUp() {
    given(integrationRepository.save(any(Integration.class))).willAnswer(new SaveAnswer<>());

    given(integrationRepository.findOne(integration.getId())).willReturn(integration);
    given(integrationRepository.findAll(pageable))
        .willReturn(new PageImpl<>(Lists.newArrayList(integration)));

    given(configurationRepository.findOne(configuration.getId())).willReturn(configuration);

    integration.export(integrationDto);

    willDoNothing().given(permissionService).canManagePcmt();
  }

  // GET /integrationProgramSchedules

  @Test
  public void shouldGetAllEntries() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("content", hasSize(1))
        .body("content.id", hasItems(integrationDto.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenTokenWasNotProvidedForGetAllEntries() {
    restAssured
        .given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForGetAllEntries() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /integrationProgramSchedules

  @Test
  public void shouldCreateNewIntegration() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(integrationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .body("id", is(not(integrationDto.getId())));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestBodyIsInvalidForCreateIntegration() {
    given(configurationRepository.findOne(configuration.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(integrationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfCronExpressionIsInvalidForCreateIntegration() {
    integrationDto.setCronExpression("* * *");

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(integrationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_CRON_EXPRESSION_INVALID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenTokenWasNotProvidedForCreateIntegration() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(integrationDto)
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForCreateIntegration() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(integrationDto)
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /integrationProgramSchedules/{id}

  @Test
  public void shouldGetIntegrationById() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("id", is(integrationDto.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenTokenWasNotProvidedForGetIntegrationById() {
    restAssured
        .given()
        .pathParam(ID, integrationDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForGetIntegrationById() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenIntegrationWithidDoesNotExistForGetIntegrationById() {
    given(integrationRepository.findOne(integrationDto.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_INTEGRATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // PUT /integrationProgramSchedules/{id}

  @Test
  public void shouldUpdateIntegration() {
    String cronExpression = "0 59 23 1 * *";
    integrationDto.setCronExpression(cronExpression);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("id", is(integrationDto.getId().toString()))
        .body("cronExpression", is(cronExpression));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewInstanceWithGivenIdIfItDoesNotExist() {
    given(integrationRepository.findOne(integration.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("id", is(integrationDto.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestBodyIsInvalidForUpdateIntegration() {
    given(configurationRepository.findOne(configuration.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfCronExpressionIsInvalidForUpdateIntegration() {
    integrationDto.setCronExpression("* * *");

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_CRON_EXPRESSION_INVALID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  @Test
  public void shouldReturnUnauthorizedWhenTokenWasNotProvidedForUpdateIntegration() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForUpdateIntegration() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationDto.getId())
        .when()
        .body(integrationDto)
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // DELETE /integrationProgramSchedules/{id}

  @Test
  public void shouldDeleteIntegration() {
    given(integrationRepository.exists(integrationDto.getId())).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(org.apache.http.HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfIntegrationDoesNotExistForDeleteIntegration() {
    given(integrationRepository.exists(integrationDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(org.apache.http.HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_INTEGRATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteIntegrationEndpointIfUserIsNotAuthorized() {
    disablePermission();

    restAssured
        .given()
        .pathParam(ID, integrationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void disablePermission() {
    willThrow(new MissingPermissionException("permission"))
        .given(permissionService)
        .canManagePcmt();
  }

}

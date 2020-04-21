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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.UUID;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.integration.pcmt.ExecutionDataBuilder;
import org.openlmis.integration.pcmt.IntegrationDataBuilder;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.i18n.MessageKeys;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.pcmt.service.referencedata.UserDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
@Ignore
public class ExecutionControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = ExecutionController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + ExecutionController.ID_URL;
  private static final String REQUEST_URL = RESOURCE_URL + ExecutionController.REQUEST_URL;

  private Execution execution = new ExecutionDataBuilder().buildAsAutomatic();
  private Execution execution1 = new ExecutionDataBuilder().buildAsManual();

  private ExecutionDto executionDto = ExecutionDto.newInstance(execution);

  private ManualIntegrationDto manualIntegrationDto = generateRequestBody();

  private ProcessingPeriodDto period =  new ProcessingPeriodDto();

  private Integration integration = new IntegrationDataBuilder().build();

  private UserDto userDto = new UserDto();

  /** Set up sample data.
   */

  @Before
  public void setUp() {

    given(executionRepository
        .findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Lists.newArrayList(execution, execution1)));

    given(integrationRepository
        .findOne(manualIntegrationDto.getIntegrationId()))
        .willReturn(integration);

    given(periodReferenceDataService
        .findOne(manualIntegrationDto.getPeriodId()))
        .willReturn(period);

    given(authenticationHelper.getCurrentUser()).willReturn(userDto);

    willDoNothing().given(permissionService).canManagePcmt();
  }

  // GET /integrationExecutions

  @Test
  public void shouldReturnPageOfExecutions() {
    given(executionRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Arrays.asList(execution, execution1)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(2))
        .body("content[0].programId", is(executionDto.getProgramId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllExecutionEndpointIfUserIsNotAuthorized() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),RamlMatchers.hasNoViolations());
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
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /integrationExecutions

  @Test
  public void shouldCreateRequest() {

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(manualIntegrationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_ACCEPTED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateRequestEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(manualIntegrationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /integrationExecutions/{id}

  @Test
  public void shouldReturnGivenExecution() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(execution);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", is(executionDto.getId().toString()))
        .body("programId", is(executionDto.getProgramId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetSpecifiedExecutionIfUserIsNotAuthorized() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(execution);

    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, executionDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForGetExecutionById() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenExecutionWithIdDoesNotExistForGetExecutionById() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_EXECUTION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /integrationExecutions/{id}/request

  @Test
  public void shouldReturnGivenExecutionRequest() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(execution);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId().toString())
        .when()
        .get(REQUEST_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(is(execution.getRequestBody()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetExecutionRequestIfUserIsNotAuthorized() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(execution);

    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, executionDto.getId().toString())
        .when()
        .get(REQUEST_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNotRightForGetExecutionRequest() {
    disablePermission();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId())
        .when()
        .get(REQUEST_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenExecutionWithIdDoesNotExistForGetExecutionRequest() {
    given(executionRepository.findOne(executionDto.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, executionDto.getId())
        .when()
        .get(REQUEST_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_EXECUTION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void disablePermission() {
    willThrow(new MissingPermissionException("permission"))
        .given(permissionService)
        .canManagePcmt();
  }

  private ManualIntegrationDto generateRequestBody() {
    ManualIntegrationDto dto = new ManualIntegrationDto();
    dto.setIntegrationId(UUID.randomUUID());
    dto.setPeriodId(UUID.randomUUID());
    dto.setFacilityId(UUID.randomUUID());
    return dto;
  }

}

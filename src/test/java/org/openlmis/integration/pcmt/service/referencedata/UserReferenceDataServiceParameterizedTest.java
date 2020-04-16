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

package org.openlmis.integration.pcmt.service.referencedata;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;
import org.openlmis.integration.pcmt.service.ResultDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(Parameterized.class)
public class UserReferenceDataServiceParameterizedTest
    extends BaseReferenceDataServiceTest<UserDto> {

  @Override
  protected BaseReferenceDataService<UserDto> getService() {
    return new UserReferenceDataService();
  }

  @Override
  protected UserDto generateInstance() {
    return new UserDto();
  }

  @Override
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    super.setUp();
  }

  private UUID user = UUID.randomUUID();
  private UUID right = UUID.randomUUID();
  private UUID program;
  private UUID facility;
  private UUID warehouse;

  /**
   * Creates new instance of Parameterized Test.
   *
   * @param program   UUID of program
   * @param facility  UUID of facility
   * @param warehouse UUID of facility
   */
  public UserReferenceDataServiceParameterizedTest(UUID program, UUID facility, UUID warehouse) {
    this.program = program;
    this.facility = facility;
    this.warehouse = warehouse;
  }

  /**
   * Get test data.
   *
   * @return collection of objects that will be passed to test constructor.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {null, null, null},
        {null, null, UUID.randomUUID()},
        {null, UUID.randomUUID(), null},
        {UUID.randomUUID(), null, null},
        {null, UUID.randomUUID(), UUID.randomUUID()},
        {UUID.randomUUID(), null, UUID.randomUUID()},
        {UUID.randomUUID(), UUID.randomUUID(), null},
        {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()}
    });
  }

  @Test
  public void shouldCheckUserRight() {
    executeHasRightEndpoint(user, right, program, facility, warehouse, true);
    executeHasRightEndpoint(user, right, program, facility, warehouse, false);
  }

  private void executeHasRightEndpoint(UUID user, UUID right, UUID program, UUID facility,
                                       UUID warehouse, boolean expectedValue) {
    // given
    UserReferenceDataService service = (UserReferenceDataService) prepareService();
    ResponseEntity<ResultDto> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET),
         any(HttpEntity.class), eq(ResultDto.class)))
        .thenReturn(response);
    when(response.getBody()).thenReturn(new ResultDto<>(expectedValue));

    ResultDto result = service.hasRight(user, right, program, facility, warehouse);

    // then
    assertThat(result.getResult(), is(expectedValue));

    verifyResultRequest()
        .isGetRequest()
        .hasAuthHeader()
        .hasEmptyBody()
        .hasQueryParameter("rightId", right.toString())
        .hasQueryParameter("programId", null != program ? program.toString() : null)
        .hasQueryParameter("facilityId", null != facility ? facility.toString() : null)
        .hasQueryParameter("warehouseId", null != warehouse ? warehouse.toString() : null);
  }

}

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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.openlmis.integration.pcmt.testbuilder.OAuth2AuthenticationDataBuilder.SERVICE_CLIENT_ID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.integration.pcmt.ObjectGenerator;
import org.openlmis.integration.pcmt.i18n.MessageKeys;
import org.openlmis.integration.pcmt.service.ResultDto;
import org.openlmis.integration.pcmt.service.referencedata.RightDto;
import org.openlmis.integration.pcmt.service.referencedata.UserDto;
import org.openlmis.integration.pcmt.service.referencedata.UserReferenceDataService;
import org.openlmis.integration.pcmt.testbuilder.OAuth2AuthenticationDataBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionServiceTest {

  private static final String PCMT_MANAGEMENT = "PCMT_MANAGEMENT";

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private PermissionService permissionService;

  private UserDto userDto;
  private RightDto rightDto;
  private OAuth2Authentication serviceAuthentication;
  private OAuth2Authentication userAuthentication;

  @Before
  public void setUp() {
    SecurityContextHolder.setContext(securityContext);

    serviceAuthentication = new OAuth2AuthenticationDataBuilder().buildServiceAuthentication();
    userAuthentication = new OAuth2AuthenticationDataBuilder().buildUserAuthentication();

    userDto = ObjectGenerator.of(UserDto.class);
    rightDto = ObjectGenerator.of(RightDto.class);

    ReflectionTestUtils.setField(permissionService, "serviceTokenClientId", SERVICE_CLIENT_ID);
  }

  @Test
  public void shouldAllowOtherServiceToManagePcmt() {
    when(securityContext.getAuthentication()).thenReturn(serviceAuthentication);

    permissionService.canManagePcmt();
  }

  @Test
  public void shouldAllowUserWithCorrectRightToManagePcmt() {
    when(securityContext.getAuthentication()).thenReturn(userAuthentication);

    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
    when(authenticationHelper.getRight(eq(PCMT_MANAGEMENT))).thenReturn(rightDto);

    when(userReferenceDataService.hasRight(userDto.getId(), rightDto.getId(), null, null, null))
        .thenReturn(new ResultDto<>(true));

    permissionService.canManagePcmt();
  }

  @Test
  public void shouldNotAllowUserWithoutCorrectRightToManagePcmt() {
    when(securityContext.getAuthentication()).thenReturn(userAuthentication);

    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
    when(authenticationHelper.getRight(eq(PCMT_MANAGEMENT))).thenReturn(rightDto);

    when(userReferenceDataService.hasRight(userDto.getId(), rightDto.getId(), null, null, null))
        .thenReturn(new ResultDto<>(false));

    expectException();

    permissionService.canManagePcmt();
  }

  private void expectException() {
    exception.expect(MissingPermissionException.class);
    exception.expectMessage(MessageKeys.ERROR_PERMISSION_MISSING);
  }

}

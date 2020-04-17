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

package org.openlmis.integration.pcmt.service.fhir;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.integration.pcmt.service.auth.AuthService;

@RunWith(MockitoJUnitRunner.class)
public class DynamicBearerTokenAuthInterceptorTest {
  private static final String TOKEN = "f4f83db4-20e0-448a-a6e3-5be219bb57aa";

  @Mock
  private AuthService authService;

  @Mock
  private IHttpRequest request;

  private DynamicBearerTokenAuthInterceptor authInterceptor;

  @Before
  public void setUp() {
    authInterceptor = new DynamicBearerTokenAuthInterceptor(authService);
  }

  @Test
  public void shouldAddGeneratedTokenToRequestHeader() {
    when(authService.obtainAccessToken("OLMIS")).thenReturn(TOKEN);

    authInterceptor.interceptRequest(request);

    verify(authService).obtainAccessToken("OLMIS");
    verify(request).addHeader(
        Constants.HEADER_AUTHORIZATION,
        Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER + TOKEN);
  }
}
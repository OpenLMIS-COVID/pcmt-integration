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

package org.openlmis.integration.pcmt.service.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Unirest.class})
public class PcmtAuthServiceTest {

  private static final String AUTHORIZATION_URL = "http://demo.akeneo.com/api/oauth/v1/token";
  private static final String TOKEN = UUID.randomUUID().toString();

  @Mock
  private Environment env;

  @Mock
  private HttpResponse<JsonNode> httpResponse;

  @Mock
  private HttpRequestWithBody httpRequestWithBody;

  @Mock
  private RequestBodyEntity requestBodyEntity;

  @Mock
  private JsonNode jsonNode;

  @Mock
  private JSONObject json;

  @InjectMocks
  private PcmtAuthService pcmtAuthService;

  private final String clientId = "2_4z3gmg1yj28008ooswkwcgg8c8wg4s00484c80so4ck8ogsogs";

  private final String clientSecret = "3ef496l42pa8sskokg0wcs80kw048co4o4wcgco804ckc0ss48";

  private final String clientUsername = "admin";

  private final String clientPassword = "password";

  private final String authorizationUrl = "http://demo.akeneo.com/api/oauth/v1/token";

  private final String base64Creds = setPlainCreds();

  @Before
  public void setUp() throws Exception {
    pcmtAuthService = new PcmtAuthService();
    PowerMockito.mockStatic(Unirest.class);
    MockitoAnnotations.initMocks(this);

    when(env.getProperty("auth.server.pcmtClientId")).thenReturn(clientId);
    when(env.getProperty("auth.server.pcmtClientSecret")).thenReturn(clientSecret);
    when(env.getProperty("auth.server.pcmtAuthorizationUrl")).thenReturn(AUTHORIZATION_URL);
    when(env.getProperty("auth.server.pcmtClientUsername")).thenReturn(clientUsername);
    when(env.getProperty("auth.server.pcmtClientPassword")).thenReturn(clientPassword);

  }

  @Test
  public void shouldObtainAccessToken() throws UnirestException {

    when(Unirest.post(authorizationUrl)).thenReturn(httpRequestWithBody);
    when(httpRequestWithBody.header("Content-Type", "application/json"))
        .thenReturn(httpRequestWithBody);
    when(httpRequestWithBody.header("Authorization", "Basic " + base64Creds))
        .thenReturn(httpRequestWithBody);
    when(httpRequestWithBody.body("{\n    \"username\" : \"" + clientUsername + "\",\n"
        + "\"password\" : \"" + clientPassword + "\",\n"
        + "\"grant_type\": \"password\"\n}")
        ).thenReturn(requestBodyEntity);
    when(requestBodyEntity.asJson()).thenReturn(httpResponse);
    when(httpResponse.getBody()).thenReturn(jsonNode);
    when(jsonNode.getObject()).thenReturn(json);
    when(json.get(PcmtAuthService.ACCESS_TOKEN)).thenReturn(TOKEN);

    String token = pcmtAuthService.obtainAccessToken();
    assertThat(token, is(equalTo(TOKEN)));

  }

  private String setPlainCreds() {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    return new String(base64CredsBytes);
  }

}



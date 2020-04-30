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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public class PcmtAuthService {

  protected static final String ACCESS_TOKEN = "access_token";

  @Autowired
  protected Environment env;

  private String clientId;

  private String clientSecret;

  private String clientUsername;

  private String clientPassword;

  private String authorizationUrl;

  private String base64Creds;

  /**
   * This method get authentication token.
   *
   * @return bearer token.
   */

  public String obtainAccessToken() {
    setClientCreds();
    setPlainCreds();

    HttpResponse<JsonNode> response = null;
    try {
      response = Unirest.post(authorizationUrl)
          .header("Content-Type", "application/json")
          .header("Authorization", "Basic " + base64Creds)
          .body("{\n    \"username\" : \"" + clientUsername + "\",\n"
              + "\"password\" : \"" + clientPassword + "\",\n"
              + "\"grant_type\": \"password\"\n}")
          .asJson();
    } catch (UnirestException e) {
      e.printStackTrace();
    }

    return response.getBody().getObject().get(ACCESS_TOKEN).toString();
  }

  private void setClientCreds() {
    clientId = env.getProperty("auth.server.pcmtClientId");
    clientSecret = env.getProperty("auth.server.pcmtClientSecret");
    authorizationUrl = env.getProperty("auth.server.pcmtAuthorizationUrl");
    clientUsername = env.getProperty("auth.server.pcmtClientUsername");
    clientPassword = env.getProperty("auth.server.pcmtClientPassword");
  }

  private void setPlainCreds() {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    base64Creds = new String(base64CredsBytes);
  }

}

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

package org.openlmis.integration.pcmt.service.pcmt;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.commons.codec.binary.Base64;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.springframework.stereotype.Service;

@Service
public class PcmtAuth extends AuthService {


  private String clientUsername;
  private String clientPassword;

  @Override
  public String obtainAccessToken() {
    setClientCreds();
    setPlainCreds();

    HttpResponse<JsonNode> response = null;
    try {
      response = Unirest.post(getAuthorizationUrl())
          .header("Content-Type", "application/json")
          .header("Authorization", "Basic " + getBase64Creds())
          .body("{\n    \"username\" : \"" + clientUsername + "\",\n    " +
              "\"password\" : \"" + clientPassword + "\",\n    " +
              "\"grant_type\": \"password\"\n}")
          .asJson();
    } catch (UnirestException e) {
      e.printStackTrace();
    }

    return response.getBody().getObject().get(AuthService.ACCESS_TOKEN).toString();
  }

  @Override
  protected void setClientCreds() {
    setClientId(env.getProperty("auth.server.pcmtClientId"));
    setClientSecret(env.getProperty("auth.server.pcmtClientSecret"));
    setAuthorizationUrl(env.getProperty("auth.server.pcmtAuthorizationUrl"));
    clientUsername = env.getProperty("auth.server.pcmtClientUsername");
    clientPassword = env.getProperty("auth.server.pcmtClientPassword");
  }

  @Override
  protected void setPlainCreds() {
    String plainCreds = getClientId() + ":" + getClientSecret();
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    setBase64Creds(new String(base64CredsBytes));
  }

}

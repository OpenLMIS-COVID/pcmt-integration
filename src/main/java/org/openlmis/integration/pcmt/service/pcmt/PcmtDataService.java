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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Setter;

import org.openlmis.integration.pcmt.service.auth.PcmtAuthService;
import org.openlmis.integration.pcmt.service.pcmt.dto.PcmtResponseBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class PcmtDataService {

  @Autowired
  private PcmtAuthService pcmtAuthService;

  @Autowired
  protected Environment env;

  @Setter
  private int pageLimit = 100;

  private String getDomainUrl() {
    return env.getProperty("pcmt.url");
  }

  public String getUrl() {
    return getDomainUrl() + "/api/rest/v1/products";
  }

  private String getToken() {
    return pcmtAuthService.obtainAccessToken();
  }

  /**
   * This method retrieves Products for given ids.
   *
   * @return List of Products.
   */

  public PcmtResponseBody downloadData(int pageNumber) {
    return getPage(pageNumber);
  }

  private PcmtResponseBody getPage(int pageNumber) {

    String url = getUrl() + "";
    PcmtResponseBody pcmtResponseBody = new PcmtResponseBody();
    try {
      Unirest.config()
          .reset()
          .verifySsl(false);

      HttpResponse<String> response =
          Unirest.get(url + "?with_count=true&page="
              + pageNumber
              + "&limit=" + pageLimit
              + "&search=%7B%22categories%22%3A%5B%7B%22operator%22%3A%22IN%22%2C%22"
              + "value%22%3A%5B%22LMIS%22%5D%7D%5D%7D"
          )
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + getToken())
          .header("Cookie", "")
          .asString();

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      pcmtResponseBody = objectMapper.readValue(response.getBody(),
          PcmtResponseBody.class);

      System.out.println(pcmtResponseBody.getLinks().getSelf());

    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return pcmtResponseBody;
  }
}

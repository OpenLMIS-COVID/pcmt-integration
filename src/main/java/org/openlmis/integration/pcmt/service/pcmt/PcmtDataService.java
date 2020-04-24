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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;

import lombok.Getter;

import org.openlmis.integration.pcmt.service.auth.PcmtAuthService;
import org.openlmis.integration.pcmt.service.pcmt.dto.PcmtResponseBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;


@Component
@SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.PreserveStackTrace"})
public class PcmtDataService {

  @Autowired
  private PcmtAuthService pcmtAuthService;

  @Value("${pcmt.url}")
  @Getter
  private String domainUrl;

  public String getUrl() {
    return domainUrl + "/api/rest/v1/products";
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
      HttpResponse<String> response =
          Unirest.get(url + "?with_count=true&page=" + pageNumber + "&limit=100"
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

    } catch (HttpStatusCodeException | UnirestException ex) {
      throw ((HttpStatusCodeException) ex);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return pcmtResponseBody;
  }
}

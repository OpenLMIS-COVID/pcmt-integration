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

import static org.junit.Assert.assertEquals;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({"PMD.TooManyMethods"})
public class PcmtDataServiceIntegrationTest {

  @Test
  public void givenArticleWhenCreatedThenCorrect() {

    Unirest.config()
        .reset()
        .verifySsl(false);

    HttpResponse<String> response = Unirest
        .post("https://demo.productcatalog.io/api/oauth/v1/token")
        .header("Content-Type", "application/json")
        .header("Authorization",
            "Basic MV81cGU0M201eHBub2trMGc0MGM4b2Nvb2M0c3Nrd3d3b2Nra3NvazRzc2s4Z2trZ280ODo2"
                + "NmR6Z3ljaGtoY3N3czB3b2NzMG8wNHM4YzRnMG9nb29vNG9zczRjYzBjYzQwMGdrOA==")
        .body("{\n    \"username\" : \"admin\",\n    \"password\" : \"Admin123\",\n    "
            + "\"grant_type\": \"password\"\n}")
        .asString();

    assertEquals(200, response.getStatus());
  }


}

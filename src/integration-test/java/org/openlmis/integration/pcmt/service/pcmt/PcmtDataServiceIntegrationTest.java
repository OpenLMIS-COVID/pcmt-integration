package org.openlmis.integration.pcmt.service.pcmt;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

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

    HttpResponse<String> response = Unirest.post("https://demo.productcatalog.io/api/oauth/v1/token")
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic MV81cGU0M201eHBub2trMGc0MGM4b2Nvb2M0c3Nrd3d3b2Nra3NvazRzc2s4Z2trZ280ODo2NmR6Z3ljaGtoY3N3czB3b2NzMG8wNHM4YzRnMG9nb29vNG9zczRjYzBjYzQwMGdrOA==")
        .body("{\n    \"username\" : \"admin\",\n    \"password\" : \"Admin123\",\n    \"grant_type\": \"password\"\n}")
        .asString();

    assertEquals(200, response.getStatus());
  }


}

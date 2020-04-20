package org.openlmis.integration.pcmt.service.pcmt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PcmtResponseBody {

//  @JsonProperty("_links")
//  public LinksDto links;
  @JsonProperty("current_page")
  public Integer currentPage;
  @JsonProperty("_embedded")
  public EmbeddedDto embedded;
}


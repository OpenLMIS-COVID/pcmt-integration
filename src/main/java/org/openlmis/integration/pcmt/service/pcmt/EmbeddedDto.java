package org.openlmis.integration.pcmt.service.pcmt;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EmbeddedDto {

  @JsonProperty("items")
  public List<ItemDto> items = null;

}
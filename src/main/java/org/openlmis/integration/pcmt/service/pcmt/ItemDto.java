package org.openlmis.integration.pcmt.service.pcmt;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ItemDto {

//  @JsonProperty("_links")
//  public Links_ links;
  @JsonProperty("identifier")
  public String identifier;
  @JsonProperty("enabled")
  public Boolean enabled;
  @JsonProperty("family")
  public String family;
  @JsonProperty("categories")
  public List<String> categories = null;
  @JsonProperty("groups")
  public List<Object> groups = null;
  @JsonProperty("parent")
  public String parent;
//  @JsonProperty("values")
//  public Values values;
  @JsonProperty("created")
  public String created;
  @JsonProperty("updated")
  public String updated;
//  @JsonProperty("associations")
//  public Associations associations;

}
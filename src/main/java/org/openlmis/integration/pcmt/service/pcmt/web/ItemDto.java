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

package org.openlmis.integration.pcmt.service.pcmt.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
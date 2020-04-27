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

package org.openlmis.integration.pcmt.testbuilder;

import java.util.List;
import org.openlmis.integration.pcmt.service.pcmt.dto.Associations;
import org.openlmis.integration.pcmt.service.pcmt.dto.Item;
import org.openlmis.integration.pcmt.service.pcmt.dto.Links;
import org.openlmis.integration.pcmt.service.pcmt.dto.Values;

public class ItemDataBuilder {

  private Links links;
  private String identifier;
  private String family;
  private Object parent;
  private List<String> categories;
  private Values values;
  private String created;
  private String updated;
  private Associations associations;

  /**
   * Returns instance of {@link ItemDataBuilder} with sample data.
   */
  public ItemDataBuilder() {
    links = null;
    identifier = null;
    family = null;
    parent = null;
    categories = null;
    values = new ValuesDataBuilder().build();
    created = null;
    updated = null;
    associations = null;
  }

  /**
   * Builds instance of {@link Item}.
   */
  public Item build() {
    return new Item(
        links,
        identifier,
        family,
        parent,
        categories,
        values,
        created,
        updated,
        associations
    );
  }

  public ItemDataBuilder withValues(Values values) {
    this.values = values;
    return this;
  }
}

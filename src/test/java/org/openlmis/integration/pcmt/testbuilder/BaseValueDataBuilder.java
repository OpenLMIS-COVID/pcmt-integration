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

import org.openlmis.integration.pcmt.service.pcmt.dto.BaseValue;

public class BaseValueDataBuilder {

  private String locale;
  private String scope;
  private String data;

  /**
   * Returns instance of {@link BaseValueDataBuilder} with sample data.
   */
  public BaseValueDataBuilder() {
    locale = null;
    scope = null;
    data = null;
  }

  /**
   * Builds instance of {@link BaseValue}.
   */
  public BaseValue build() {
    return new BaseValue(locale, scope, data);
  }

  public BaseValueDataBuilder withData(String data) {
    this.data = data;
    return this;
  }
}

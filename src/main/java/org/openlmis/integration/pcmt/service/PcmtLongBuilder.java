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

package org.openlmis.integration.pcmt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class PcmtLongBuilder {

  @Value("${pcmt.decimalSeparator}")
  private String decimalSeparator;

  @Value("${pcmt.groupingSeparator}")
  private String groupingSeparator;

  /**
   * Builds Long from the String returned by PCMT. Uses decimal and grouping separators
   * defined in the application.properties, i.e. 1,000.0000 = 1000 or 1.0000 = 1.
   *
   * @param number number string returned by PCMT
   */
  public Long build(String number) {
    String decimal = number;
    if (number.contains(decimalSeparator)) {
      decimal = number.substring(0, number.indexOf(decimalSeparator));
    }
    String formatted = decimal.replace(groupingSeparator, "");
    return Long.valueOf(formatted);
  }

}

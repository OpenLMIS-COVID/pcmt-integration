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
import java.util.UUID;
import org.javers.common.collections.Lists;
import org.openlmis.integration.pcmt.service.pcmt.dto.BaseValue;
import org.openlmis.integration.pcmt.service.pcmt.dto.PriceReference;
import org.openlmis.integration.pcmt.service.pcmt.dto.Values;

public class ValuesDataBuilder {
  private static int instanceNumber = 0;

  private List<BaseValue> packSize;
  private List<PriceReference> priceReferences;
  private List<BaseValue> baseUom;
  private List<BaseValue> lmisCode;
  private List<BaseValue> lmisUuid;
  private List<BaseValue> productDescription;
  private List<BaseValue> uomQtyFactor;
  private List<BaseValue> lmisPackRoundingThreshold;
  private List<BaseValue> lmisRoundToZero;

  /**
   * Returns instance of {@link ValuesDataBuilder} with sample data.
   */
  public ValuesDataBuilder() {
    instanceNumber++;

    packSize = null;
    priceReferences = null;
    baseUom = null;
    lmisCode =
        Lists.asList(
            new BaseValueDataBuilder().withData("COV" + instanceNumber).build());
    lmisUuid = Lists.asList(
        new BaseValueDataBuilder().withData(UUID.randomUUID().toString()).build());
    productDescription =
        Lists.asList(
            new BaseValueDataBuilder().withData("Gloves" + instanceNumber).build());
    uomQtyFactor =
        Lists.asList(
            new BaseValueDataBuilder().withData("10.000").build());
    lmisPackRoundingThreshold = Lists.asList(
        new BaseValueDataBuilder().withData("4").build());
    lmisRoundToZero = Lists.asList(
        new BaseValueDataBuilder().withData("true").build());
  }

  /**
   * Builds instance of {@link Values}.
   */
  public Values build() {
    return new Values(
        packSize,
        priceReferences,
        baseUom,
        lmisCode,
        lmisUuid,
        productDescription,
        uomQtyFactor,
        lmisPackRoundingThreshold,
        lmisRoundToZero
    );
  }

  public ValuesDataBuilder withoutLmisUuid() {
    this.lmisUuid = null;
    return this;
  }
}

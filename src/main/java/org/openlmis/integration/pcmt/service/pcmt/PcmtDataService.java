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

import org.openlmis.integration.pcmt.service.BaseCommunicationService;
import org.openlmis.integration.pcmt.service.RequestParameters;
import org.springframework.beans.factory.annotation.Value;

public class PcmtDataService extends BaseCommunicationService {

  @Override
  protected String getServiceUrl() {
    return null;
  }

  @Value("${pcmt.url}")
  private String referenceDataUrl;

  @Override
  protected String getUrl() {
    return referenceDataUrl + "/api/rest/v1/product-models";
  }

  @Override
  protected Class getResultClass() {
    return Object.class;
  }

  @Override
  protected Class getArrayResultClass() {
    return Object[].class;
  }

  /**
   * This method retrieves Products for given ids.
   *
   * @return List of Products.
   */
  public Object search() {
    return getPage(RequestParameters.init()).getContent();
  }



}

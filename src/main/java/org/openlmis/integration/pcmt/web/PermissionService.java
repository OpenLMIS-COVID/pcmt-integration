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

package org.openlmis.integration.pcmt.web;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import org.openlmis.integration.pcmt.service.ResultDto;
import org.openlmis.integration.pcmt.service.referencedata.RightDto;
import org.openlmis.integration.pcmt.service.referencedata.UserDto;
import org.openlmis.integration.pcmt.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
class PermissionService {

  private static final String PCMT_MANAGEMENT = "PCMT_MANAGEMENT";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Value("${auth.server.olmisClientId}")
  private String serviceTokenClientId;

  void canManagePcmt() {
    if (hasNoPermission(PCMT_MANAGEMENT, true)) {
      throw new MissingPermissionException(PCMT_MANAGEMENT);
    }
  }

  private boolean hasNoPermission(String rightName, boolean allowUsers) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    if (authentication.isClientOnly()) {
      return isNotValidServiceToken(authentication);
    }

    if (allowUsers) {
      return isNotValidUserToken(rightName);
    }

    return true;
  }

  private boolean isNotValidUserToken(String rightName) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
        user.getId(), right.getId(), null, null, null
    );

    return null == result || isNotTrue(result.getResult());
  }

  private boolean isNotValidServiceToken(OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();
    return !serviceTokenClientId.equals(clientId);
  }

}

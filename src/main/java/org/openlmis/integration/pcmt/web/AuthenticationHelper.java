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

import static org.openlmis.integration.pcmt.i18n.MessageKeys.ERROR_RIGHT_NOT_FOUND;
import static org.openlmis.integration.pcmt.i18n.MessageKeys.ERROR_USER_NOT_FOUND;

import java.util.UUID;
import org.openlmis.integration.pcmt.service.referencedata.RightDto;
import org.openlmis.integration.pcmt.service.referencedata.RightReferenceDataService;
import org.openlmis.integration.pcmt.service.referencedata.UserDto;
import org.openlmis.integration.pcmt.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
class AuthenticationHelper {

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  /**
   * Method returns current user based on Spring context and fetches his data from reference-data
   * service.
   *
   * @return UserDto entity of current user.
   * @throws AuthenticationException if user cannot be found.
   */
  UserDto getCurrentUser() {
    OAuth2Authentication authentication =
        (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
    UserDto user = null;

    if (!authentication.isClientOnly()) {
      UUID userId = (UUID) authentication.getPrincipal();
      user = userReferenceDataService.findOne(userId);

      if (user == null) {
        throw new AuthenticationException(ERROR_USER_NOT_FOUND, userId.toString());
      }
    }

    return user;
  }

  /**
   * Method returns a correct right and fetches his data from reference-data service.
   *
   * @param name right name
   * @return RightDto entity of right.
   * @throws AuthenticationException if right cannot be found.
   */
  RightDto getRight(String name) {
    RightDto right = rightReferenceDataService.findRight(name);

    if (null == right) {
      throw new AuthenticationException(ERROR_RIGHT_NOT_FOUND, name);
    }

    return right;
  }
}
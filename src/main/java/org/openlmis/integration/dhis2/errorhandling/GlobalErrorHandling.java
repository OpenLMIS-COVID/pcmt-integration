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

package org.openlmis.integration.dhis2.errorhandling;

import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_CONFIGURATION_NAME_DUPLICATED;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_CONFIGURATION_TARGET_URL_DUPLICATED;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_INTEGRATION_DUPLICATED;
import static org.openlmis.integration.dhis2.i18n.MessageKeys.ERROR_INTEGRATION_PROGRAM_ID_DUPLICATED;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.util.Message;
import org.openlmis.integration.dhis2.web.MissingPermissionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global error handling for all controllers in the service. Contains common error handling
 * mappings.
 */
@ControllerAdvice
public class GlobalErrorHandling extends AbstractErrorHandling {

  private static final Map<String, String> CONSTRAINT_MAP = new HashMap<>();

  static {
    CONSTRAINT_MAP.put("configuration_name_unique", ERROR_CONFIGURATION_NAME_DUPLICATED);
    CONSTRAINT_MAP
        .put("configuration_target_url_unique", ERROR_CONFIGURATION_TARGET_URL_DUPLICATED);

    CONSTRAINT_MAP
        .put("integration_programid_configurationid_unq", ERROR_INTEGRATION_PROGRAM_ID_DUPLICATED);
    CONSTRAINT_MAP.put("integration_configurationid_unq", ERROR_INTEGRATION_DUPLICATED);
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public Message.LocalizedMessage handleNotFoundException(NotFoundException ex) {
    return getLocalizedMessage(ex);
  }

  /**
   * Handles Message exceptions and returns status 400 Bad Request.
   *
   * @param ex the ValidationMessageException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Message.LocalizedMessage handleMessageException(ValidationMessageException ex) {
    return getLocalizedMessage(ex);
  }

  /**
   * Handles data integrity violation exception.
   *
   * @param ex the data integrity exception
   * @return the user-oriented error message.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Message.LocalizedMessage handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    if (ex.getCause() instanceof ConstraintViolationException) {
      ConstraintViolationException cause = (ConstraintViolationException) ex.getCause();
      String messageKey = CONSTRAINT_MAP.get(cause.getConstraintName());
      if (messageKey != null) {
        return getLocalizedMessage(new Message(messageKey));
      }
    }

    return getLocalizedMessage(new Message(ex.getMessage()));
  }

  /**
   * Handles the {@link MissingPermissionException} which signals unauthorized access.
   *
   * @return the localized message
   */
  @ExceptionHandler(MissingPermissionException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public Message.LocalizedMessage handleMissingPermissionException(MissingPermissionException ex) {
    return getLocalizedMessage(ex.asMessage());
  }
}

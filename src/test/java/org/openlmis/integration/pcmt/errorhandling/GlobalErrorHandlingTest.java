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

package org.openlmis.integration.pcmt.errorhandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.integration.pcmt.i18n.MessageKeys.ERROR_PERMISSION_MISSING;

import java.util.Locale;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.integration.pcmt.exception.NotFoundException;
import org.openlmis.integration.pcmt.exception.ValidationMessageException;
import org.openlmis.integration.pcmt.i18n.MessageService;
import org.openlmis.integration.pcmt.util.Message;
import org.openlmis.integration.pcmt.util.Message.LocalizedMessage;
import org.openlmis.integration.pcmt.web.MissingPermissionException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;

@RunWith(MockitoJUnitRunner.class)
public class GlobalErrorHandlingTest {

  private static final Locale ENGLISH_LOCALE = Locale.ENGLISH;
  private static final String ERROR_MESSAGE = "error-message";
  private static final String MESSAGE_KEY = "key";

  @Mock
  private MessageService messageService;

  @Mock
  private MessageSource messageSource;

  @InjectMocks
  private GlobalErrorHandling errorHandler;

  @Before
  public void setUp() {
    when(messageService.localize(any(Message.class)))
        .thenAnswer(invocation -> {
          Message message = invocation.getArgumentAt(0, Message.class);
          return message.localMessage(messageSource, ENGLISH_LOCALE);
        });
  }


  @Test
  public void shouldHandleDataIntegrityViolationEvenIfMessageKeyNotExist() {
    // given
    String constraintName = "unq_widget_code_def";
    ConstraintViolationException constraintViolation = new ConstraintViolationException(
        null, null, constraintName);
    DataIntegrityViolationException exp = new DataIntegrityViolationException(
        null, constraintViolation);

    // when
    mockMessage(exp.getMessage());
    LocalizedMessage message = errorHandler.handleDataIntegrityViolation(exp);

    // then
    assertMessage(message, exp.getMessage());
  }

  @Test
  public void shouldHandleDataIntegrityViolationEvenIfCauseNotExist() {
    // given
    DataIntegrityViolationException exp = new DataIntegrityViolationException(ERROR_MESSAGE, null);

    // when
    mockMessage(exp.getMessage());
    LocalizedMessage message = errorHandler.handleDataIntegrityViolation(exp);

    // then
    assertMessage(message, exp.getMessage());
  }

  @Test
  public void shouldHandleMessageException() {
    // given
    ValidationMessageException exp = new ValidationMessageException(MESSAGE_KEY);

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handleMessageException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandleNotFoundException() {
    // given
    NotFoundException exp = new NotFoundException(MESSAGE_KEY);

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handleNotFoundException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandleMissingPermissionException() {
    // given
    MissingPermissionException exp = new MissingPermissionException(MESSAGE_KEY);

    // when
    mockMessage(ERROR_PERMISSION_MISSING, MESSAGE_KEY);
    Message.LocalizedMessage message = errorHandler.handleMissingPermissionException(exp);

    // then
    assertMessage(message, ERROR_PERMISSION_MISSING);
  }

  private void assertMessage(Message.LocalizedMessage localized, String key) {
    assertThat(localized)
        .hasFieldOrPropertyWithValue("messageKey", key);
    assertThat(localized)
        .hasFieldOrPropertyWithValue("message", ERROR_MESSAGE);
  }

  private void mockMessage(String key, String... params) {
    if (params.length == 0) {
      when(messageSource.getMessage(key, new Object[0], ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
      when(messageSource.getMessage(key, null, ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
    } else {
      when(messageSource.getMessage(key, params, ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
    }
  }
}

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

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.exception.NotFoundException;
import org.openlmis.integration.pcmt.exception.ValidationMessageException;
import org.openlmis.integration.pcmt.i18n.MessageKeys;
import org.openlmis.integration.pcmt.repository.ConfigurationRepository;
import org.openlmis.integration.pcmt.repository.IntegrationRepository;
import org.openlmis.integration.pcmt.scheduler.DynamicTaskScheduler;
import org.openlmis.integration.pcmt.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(IntegrationController.RESOURCE_PATH)
public class IntegrationController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/integrationProgramSchedules";
  public static final String ID_URL = "/{id}";

  @Autowired
  private DynamicTaskScheduler scheduler;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private ConfigurationRepository configurationRepository;

  /**
   * Gets all integrations available in the system.
   *
   * @param pageable define which page and how many records should be returned.
   */
  @GetMapping
  public Page<IntegrationDto> getAllIntegrations(Pageable pageable) {
    permissionService.canManagePcmt();

    Page<Integration> page = integrationRepository.findAll(pageable);
    List<IntegrationDto> content = page
        .getContent()
        .stream()
        .map(IntegrationDto::newInstance)
        .collect(Collectors.toList());

    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Creates a new integration based on passed data.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public IntegrationDto createIntegration(@RequestBody IntegrationDto dto) {
    permissionService.canManagePcmt();

    Configuration configuration = configurationRepository.findOne(dto.getConfigurationId());

    if (null == configuration) {
      throw new ValidationMessageException(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND);
    }
    validateCronExpression(dto.getCronExpression());

    Integration integration = new Integration();
    integration.updateFrom(dto);
    integration.setConfiguration(configuration);

    integrationRepository.saveAndFlush(integration);
    scheduler.refresh();

    return IntegrationDto.newInstance(integration);
  }

  /**
   * Gets a single integration based on passed ID value.
   */
  @GetMapping(ID_URL)
  public IntegrationDto getIntegration(@PathVariable("id") UUID id) {
    permissionService.canManagePcmt();

    Integration integration = integrationRepository.findOne(id);

    if (null == integration) {
      throw new NotFoundException(MessageKeys.ERROR_INTEGRATION_NOT_FOUND);
    }

    return IntegrationDto.newInstance(integration);
  }

  /**
   * Updates an existing integration with the given ID value or create a new integration with the
   * given ID value.
   */
  @PutMapping(ID_URL)
  public IntegrationDto updateOrCreateIntegration(@PathVariable("id") UUID id,
      @RequestBody IntegrationDto dto) {
    permissionService.canManagePcmt();

    if (null != dto.getId() && !Objects.equals(dto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_INTEGRATION_ID_MISMATCH);
    }
    validateCronExpression(dto.getCronExpression());

    Configuration configuration = configurationRepository.findOne(dto.getConfigurationId());

    if (null == configuration) {
      throw new ValidationMessageException(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND);
    }

    Integration integration = integrationRepository.findOne(id);

    if (null == integration) {
      integration = new Integration();
      integration.setId(id);
    }

    integration.updateFrom(dto);
    integration.setConfiguration(configuration);

    integrationRepository.saveAndFlush(integration);
    scheduler.refresh();

    return IntegrationDto.newInstance(integration);
  }

  /**
   * Delete chosen integration.
   *
   * @param id UUID of integration item which we want to delete.
   */
  @DeleteMapping(value = ID_URL)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteIntegration(@PathVariable("id") UUID id) {
    permissionService.canManagePcmt();

    if (!integrationRepository.exists(id)) {
      throw new NotFoundException(MessageKeys.ERROR_INTEGRATION_NOT_FOUND);
    }
    integrationRepository.delete(id);
    scheduler.refresh();
  }

  private void validateCronExpression(String cronExpression) {
    if (null == cronExpression) {
      throw new ValidationMessageException(MessageKeys.ERROR_CRON_EXPRESSION_MISSING);
    }

    try {
      // the following constructor tries to parse the passed cron expression
      // and throws an IllegalArgumentException exception if it cannot be parsed.
      new CronSequenceGenerator(cronExpression);
    } catch (IllegalArgumentException exp) {
      throw new ValidationMessageException(exp,
          MessageKeys.ERROR_CRON_EXPRESSION_INVALID, cronExpression);
    }
  }

}

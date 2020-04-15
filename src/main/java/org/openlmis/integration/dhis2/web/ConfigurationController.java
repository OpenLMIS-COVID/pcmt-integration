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

package org.openlmis.integration.dhis2.web;

import static org.openlmis.integration.dhis2.web.ConfigurationController.RESOURCE_PATH;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlmis.integration.dhis2.domain.Configuration;
import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationDetails;
import org.openlmis.integration.dhis2.domain.ConfigurationAuthenticationType;
import org.openlmis.integration.dhis2.exception.NotFoundException;
import org.openlmis.integration.dhis2.exception.ValidationMessageException;
import org.openlmis.integration.dhis2.i18n.MessageKeys;
import org.openlmis.integration.dhis2.repository.ConfigurationRepository;
import org.openlmis.integration.dhis2.repository.IntegrationRepository;
import org.openlmis.integration.dhis2.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping(RESOURCE_PATH)
public class ConfigurationController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/integrationConfigurations";
  public static final String ID_URL = "/{id}";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ConfigurationRepository configurationRepository;

  @Autowired
  private IntegrationRepository integrationRepository;

  /**
   * This method is used to add new Configuration.
   */

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ConfigurationDto createNewConfiguration(
      @RequestBody ConfigurationDto configurationDto) {
    permissionService.canManageDhis2();

    ConfigurationAuthenticationDetailsDto confAuthDetailsDto =
        configurationDto.getAuthenticationDetails();

    ConfigurationAuthenticationDetails confAuthDetails = null;

    try {
      new URL(configurationDto.getTargetUrl()).toURI();
    } catch (Exception e) {
      throw new ValidationMessageException(e, MessageKeys.ERROR_TARGET_URL_INVALID);
    }

    if (confAuthDetailsDto.getType() != null) {
      if (confAuthDetailsDto.getType().equals(ConfigurationAuthenticationType.BEARER)) {
        confAuthDetails = new ConfigurationAuthenticationDetails(confAuthDetailsDto.getToken());
      }
      if (confAuthDetailsDto.getType().equals(ConfigurationAuthenticationType.BASIC)) {
        confAuthDetails = new ConfigurationAuthenticationDetails(confAuthDetailsDto.getUsername(),
            confAuthDetailsDto.getPassword());
      }
    }

    Configuration configuration = new Configuration(configurationDto.getName(),
        configurationDto.getTargetUrl(),confAuthDetails);

    configurationRepository.saveAndFlush(configuration);

    return ConfigurationDto.newInstance(configuration);
  }

  /**
   * Retrieves all configurations. Note that an empty collection rather than a 404 should be
   * returned if no configurations exist.
   *
   *  @param pageable define which page and how many records should be returned.
   */

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ConfigurationDto> getAllConfigurations(Pageable pageable) {
    permissionService.canManageDhis2();

    Page<Configuration> page = configurationRepository.findAll(pageable);
    List<ConfigurationDto> content = page
        .getContent()
        .stream()
        .map(ConfigurationDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves configuration based on passed ID value.
   */
  @GetMapping(value = ID_URL)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ConfigurationDto getSpecifiedConfiguration(@PathVariable("id") UUID id) {
    permissionService.canManageDhis2();

    Configuration configuration = configurationRepository.findOne(id);

    if (configuration == null) {
      throw new NotFoundException(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND);
    }
    return ConfigurationDto.newInstance(configuration);
  }

  /**
   * Update existing configuration with the given ID value or create a new configuration with the
   * given ID value.
   */

  @PutMapping(value = ID_URL)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ConfigurationDto updateExistingConfiguration(@PathVariable("id") UUID id,
      @RequestBody ConfigurationDto configurationDto) {
    permissionService.canManageDhis2();

    if (null != configurationDto.getId() && !Objects.equals(configurationDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_CONFIGURATION_ID_MISMATCH);
    }
    Configuration configuration = configurationRepository.findOne(id);

    if (null == configuration) {
      configuration = new Configuration();
      configuration.setId(id);
    }

    configuration.updateFrom(configurationDto);
    configurationRepository.saveAndFlush(configuration);

    return configurationDto;
  }

  /**
   * Delete chosen configuration.
   *
   * @param id UUID of configuration item which we want to delete.
   */
  @DeleteMapping(value = ID_URL)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteConfiguration(@PathVariable("id") UUID id) {
    permissionService.canManageDhis2();

    if (!configurationRepository.exists(id)) {
      throw new NotFoundException(MessageKeys.ERROR_CONFIGURATION_NOT_FOUND);
    }
    if (integrationRepository.existsByConfiguration_Id(id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_CONFIGURATION_USED);
    }
    configurationRepository.delete(id);
  }

}

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

import static org.openlmis.integration.pcmt.web.ExecutionController.RESOURCE_PATH;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.exception.NotFoundException;
import org.openlmis.integration.pcmt.i18n.MessageKeys;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.repository.IntegrationRepository;
import org.openlmis.integration.pcmt.service.PayloadRequest;
import org.openlmis.integration.pcmt.service.PayloadService;
import org.openlmis.integration.pcmt.service.referencedata.PeriodReferenceDataService;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;
import org.openlmis.integration.pcmt.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@Transactional
@RestController
@RequestMapping(RESOURCE_PATH)
public class ExecutionController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/integrationExecutions";
  public static final String ID_URL = "/{id}";
  public static final String REQUEST_URL = ID_URL + "/request";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PayloadService payloadService;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private ExecutionRepository executionRepository;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  /**
   * This method is used to manual trigger Integration.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void runManualIntegration(@RequestBody ManualIntegrationDto manualIntegrationDto) {
    permissionService.canManageDhis2();

    Integration integration = integrationRepository
        .findOne(manualIntegrationDto.getIntegrationId());
    if (null == integration) {
      throw new NotFoundException(MessageKeys.ERROR_INTEGRATION_NOT_FOUND);
    }

    ProcessingPeriodDto period = periodReferenceDataService
        .findOne(manualIntegrationDto.getPeriodId());
    if (null == period) {
      throw new NotFoundException(MessageKeys.ERROR_PERIOD_NOT_FOUND);
    }
    UUID userId = authenticationHelper.getCurrentUser().getId();

    PayloadRequest payloadRequest = PayloadRequest.forManualExecution(integration,
        manualIntegrationDto.getFacilityId(), period,
        manualIntegrationDto.getDescription(), userId);

    payloadService.postPayload(payloadRequest);

  }

  /**
   * Retrieves all historical executions. Note that an empty collection rather than a 404 should be
   * returned if no historical executions exist.
   *
   * @param pageable define which page and how many records should be returned.
   */
  @GetMapping
  public Page<ExecutionDto> getAllHistoricalExecutions(Pageable pageable) {
    permissionService.canManageDhis2();

    Page<Execution> page = executionRepository.findAll(pageable);
    List<ExecutionDto> content = page
        .getContent()
        .stream()
        .map(ExecutionDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the historical execution based on passed ID value.
   */
  @GetMapping(ID_URL)
  public ExecutionDto getSpecifiedHistoricalExecution(@PathVariable("id") UUID id) {
    permissionService.canManageDhis2();

    Execution execution = executionRepository.findOne(id);
    if (execution == null) {
      throw new NotFoundException(MessageKeys.ERROR_EXECUTION_NOT_FOUND);
    }
    return ExecutionDto.newInstance(execution);
  }

  /**
   * Retrieves the request that has been used with the given execution.
   */
  @GetMapping(value = REQUEST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
  public String getExecutionRequest(@PathVariable("id") UUID id) {
    permissionService.canManageDhis2();

    Execution execution = executionRepository.findOne(id);
    if (execution == null) {
      throw new NotFoundException(MessageKeys.ERROR_EXECUTION_NOT_FOUND);
    }

    return execution.getRequestBody();
  }
}

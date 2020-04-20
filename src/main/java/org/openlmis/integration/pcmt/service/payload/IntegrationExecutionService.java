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

package org.openlmis.integration.pcmt.service.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.PayloadBuilder;
import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.referencedata.OrderableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class IntegrationExecutionService {

  @Autowired
  private Clock clock;

  @Autowired
  private PayloadBuilder payloadBuilder;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ExecutionRepository executionRepository;

  @Autowired
  private IntegrationExecutor integrationExecutor;

  @Autowired
  private PcmtDataService pcmtDataService;

  /**
   * Method is responsible for sending payload to Interop layer. Response is a status (202, 500 or
   * 503), message and notificationsChannel.
   */
  public void integrate(UUID userId, Integration integration, boolean manualExecution) {
    List<Object> items = pcmtDataService.search();

    for (Object item : items) {
      integrationExecutor.execute(getTask(
          integration, manualExecution, (OrderableDto) item, userId));
    }
  }

  private IntegrationTask<OrderableDto> getTask(Integration integration, boolean manualExecution,
      OrderableDto entity, UUID userId) {

    try {
      ExecutableRequest<OrderableDto> request = new ExecutableRequest<>(manualExecution);
      request.setRequestEntity(entity, HttpMethod.PUT, new URI(pcmtDataService.getUrl()));

      return new OrderableIntegrationTask(entity, request, integration, userId, executionRepository,
          clock, payloadBuilder,  objectMapper);
    } catch (URISyntaxException e) {
      return null; // TODO COV-29: add error handling inside task
    }
  }

}

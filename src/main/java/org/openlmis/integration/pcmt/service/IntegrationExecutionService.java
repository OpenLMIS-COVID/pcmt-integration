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

package org.openlmis.integration.pcmt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.ExecutionRepository;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.openlmis.integration.pcmt.service.fetch.IntegrationFetchExecutor;
import org.openlmis.integration.pcmt.service.fetch.OrderableIntegrationFetchTask;
import org.openlmis.integration.pcmt.service.pcmt.PcmtDataService;
import org.openlmis.integration.pcmt.service.referencedata.orderable.OrderableDto;
import org.openlmis.integration.pcmt.service.send.IntegrationSendExecutor;
import org.openlmis.integration.pcmt.service.send.IntegrationSendTask;
import org.openlmis.integration.pcmt.service.send.OrderableIntegrationSendTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IntegrationExecutionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationExecutionService.class);

  @Value("${referencedata.url}")
  private String targetUrl;

  @Autowired
  private Clock clock;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ExecutionRepository executionRepository;

  @Autowired
  private IntegrationSendExecutor integrationSendExecutor;

  @Autowired
  private IntegrationFetchExecutor integrationFetchExecutor;

  @Autowired
  private AuthService authService;

  @Autowired
  private PcmtDataService pcmtDataService;

  @Autowired
  private PcmtLongBuilder pcmtLongBuilder;

  private final BlockingQueue<OrderableDto> queue = new LinkedBlockingDeque<>();

  /**
   * Integrates the PCMT with the OpenLMIS system. Designed for scheduled executions.
   *
   * @param integration configuration
   */
  public void integrate(Integration integration) {
    LOGGER.info("Scheduled Integration {} was started by a scheduler", integration.getId());
    integrate(null, integration,false);
  }

  /**
   * Integrates the PCMT with the OpenLMIS system. Designed for manual executions.
   *
   * @param userId of a user who started integration manually
   * @param integration configuration
   */
  public void integrate(UUID userId, Integration integration) {
    LOGGER.info("Manual integration with id: {} was started by a user with id: {}",
        integration.getId(), userId);
    integrate(userId, integration,true);
  }

  private void integrate(UUID userId, Integration integration, boolean manualExecution) {

    OrderableIntegrationFetchTask producer = new OrderableIntegrationFetchTask(pcmtDataService,
        pcmtLongBuilder, queue, clock);

    IntegrationSendTask<OrderableDto> consumer = new OrderableIntegrationSendTask(
        queue, integration, userId, targetUrl, manualExecution,
        executionRepository, clock, objectMapper, authService);

    LOGGER.info("Integration {} was started by a user with id: {}", integration.getId(), userId);

    integrationFetchExecutor.execute(producer);
    integrationSendExecutor.execute(consumer);
  }
}

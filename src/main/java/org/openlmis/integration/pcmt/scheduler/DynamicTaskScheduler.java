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

package org.openlmis.integration.pcmt.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.openlmis.integration.pcmt.domain.Integration;
import org.openlmis.integration.pcmt.repository.IntegrationRepository;
import org.openlmis.integration.pcmt.service.PayloadRequest;
import org.openlmis.integration.pcmt.service.PayloadService;
import org.openlmis.integration.pcmt.service.referencedata.PeriodReferenceDataService;
import org.openlmis.integration.pcmt.service.referencedata.ProcessingPeriodDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class DynamicTaskScheduler implements SchedulingConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTaskScheduler.class);

  @Value("${pcmt.enableAutoSend}")
  private boolean enableAutoSend;

  @Autowired
  private PayloadService payloadService;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  private ScheduledTaskRegistrar taskRegistrar;
  private Clock clock;
  private TimeZone timeZone;

  /**
   * Creates new task by cron expressions from DB.
   */
  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    this.taskRegistrar = taskRegistrar;
    this.taskRegistrar.setScheduler(poolScheduler());

    refresh(true);
  }

  public void refresh() {
    refresh(false);
  }

  private void refresh(boolean initialization) {
    if (!enableAutoSend) {
      LOGGER.warn("Auto sending data is disabled");
      return;
    }

    LOGGER.info("Destroying current cron tasks");
    taskRegistrar.destroy();
    LOGGER.info("Destroyed current cron tasks");

    LOGGER.info("Create new cron tasks");
    List<CronTask> tasks = integrationRepository
        .findAll()
        .stream()
        .collect(Collectors.groupingBy(Integration::getCronExpression))
        .entrySet()
        .stream()
        .map(this::createTask)
        .collect(Collectors.toList());

    taskRegistrar.setCronTasksList(tasks);
    LOGGER.info("Set new cron tasks");

    if (!initialization) {
      // when the service is initialized by spring the following method does not need to be call
      // because the framework will do this for us but the method needs to be called after each
      // change in any integration entry.
      taskRegistrar.afterPropertiesSet();
    }
  }

  private CronTask createTask(Map.Entry<String, List<Integration>> entry) {
    LOGGER.info(
        "Create task for >{}< cron expression (integration count: {})",
        entry.getKey(), entry.getValue().size());

    CronTrigger trigger = new CronTrigger(entry.getKey(), timeZone);
    Runnable task = () -> sendData(entry.getValue());

    return new CronTask(task, trigger);
  }

  @Autowired
  public void setClock(Clock clock) {
    this.clock = clock;
    this.timeZone = TimeZone.getTimeZone(clock.getZone());
  }

  private TaskScheduler poolScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    scheduler.setPoolSize(1);
    scheduler.initialize();

    return scheduler;
  }

  /**
   * Place for init tasks.
   */
  private void sendData(List<Integration> integrations) {
    LOGGER.debug("Send data for {} integrations", integrations.size());

    LocalDate now = LocalDate.now(clock);
    LocalDate nowMinusMonth = now.minusMonths(1);
    LocalDate startDate = nowMinusMonth.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDate = nowMinusMonth.with(TemporalAdjusters.lastDayOfMonth());

    LOGGER.debug("Current date: {}", now);
    LOGGER.trace("Previous date: {}", nowMinusMonth);
    LOGGER.trace("First day of month: {}", startDate);
    LOGGER.trace("Last day of month: {}", endDate);

    ProcessingPeriodDto period = periodReferenceDataService.search(startDate, endDate).get(0);
    LOGGER.info("Retrieved period: {}", period.getName());

    for (Integration integration : integrations) {
      sendData(integration, period);
    }

    LOGGER.debug("Sent data for {} integrations", integrations.size());
  }

  private void sendData(Integration integration, ProcessingPeriodDto period) {
    if (null == integration.getProgramId()) {
      LOGGER.info("Send data for all programs for {} period", period.getName());
    } else {
      LOGGER.info(
          "Send data for program {} for {} period",
          integration.getProgramId(), period.getName()
      );
    }

    PayloadRequest request = PayloadRequest.forAutomaticExecution(integration, period);
    payloadService.postPayload(request);
  }

}

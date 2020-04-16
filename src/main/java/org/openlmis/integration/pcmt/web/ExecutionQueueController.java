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

import static org.openlmis.integration.pcmt.web.ExecutionQueueController.RESOURCE_PATH;

import java.util.Set;
import java.util.stream.Collectors;
import org.openlmis.integration.pcmt.service.PostPayloadTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Transactional
@RestController
@RequestMapping(RESOURCE_PATH)
public class ExecutionQueueController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/integrationExecutionQueue";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PostPayloadTaskExecutor postPayloadTaskExecutor;

  /**
   * Retrieves executions from the execution queue.
   */
  @GetMapping
  public Set<PostPayloadTaskDto> getExecutionsInQueue() {
    permissionService.canManagePcmt();
    return postPayloadTaskExecutor
        .getQueueItems()
        .stream()
        .map(PostPayloadTaskDto::newInstance)
        .collect(Collectors.toSet());
  }

}

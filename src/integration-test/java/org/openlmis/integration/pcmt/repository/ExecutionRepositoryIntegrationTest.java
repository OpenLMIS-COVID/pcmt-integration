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

package org.openlmis.integration.pcmt.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import org.openlmis.integration.pcmt.IntegrationDataBuilder;
import org.openlmis.integration.pcmt.domain.Execution;
import org.openlmis.integration.pcmt.domain.ExecutionResponse;
import org.openlmis.integration.pcmt.domain.Integration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class ExecutionRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Execution> {

  private static final Integration INTEGRATION = new IntegrationDataBuilder().build();
  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Autowired
  private ExecutionRepository repository;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  CrudRepository<Execution, UUID> getRepository() {
    return repository;
  }

  @Override
  Execution generateInstance() {
    return Execution.forAutomaticExecution(INTEGRATION, CLOCK);
  }

  @Test
  public void shouldCreateWithResponse() {
    // given
    Execution execution = generateInstance();
    repository.saveAndFlush(execution);

    ExecutionResponse response = new ExecutionResponse(ZonedDateTime.now(CLOCK), 200, "ok");
    execution.markAsDone(response, CLOCK);

    // when
    repository.saveAndFlush(execution);

    // then
    assertThat(repository.exists(execution.getId()))
        .isTrue();

    assertThat(entityManager
        .createQuery("SELECT er FROM ExecutionResponse AS er where er.id = :id")
        .setParameter("id", execution.getId())
        .getSingleResult())
        .isNotNull();
  }
}

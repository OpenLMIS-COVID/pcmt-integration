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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.integration.pcmt.ConfigurationDataBuilder;
import org.openlmis.integration.pcmt.domain.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

@Ignore
public class ConfigurationRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Configuration> {

  @Autowired
  private ConfigurationRepository repository;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  CrudRepository<Configuration, UUID> getRepository() {
    return repository;
  }

  @Override
  Configuration generateInstance() {
    return new ConfigurationDataBuilder()
        .withoutCredentials()
        .buildAsNew();
  }

  @Test
  public void shouldCreateWithCredentials() {
    // given
    Configuration configuration = new ConfigurationDataBuilder()
        .withCredentials(UUID.randomUUID().toString())
        .buildAsNew();

    // when
    repository.save(configuration);

    // then
    assertThat(repository.exists(configuration.getId()))
        .isTrue();

    assertThat(entityManager
        .createQuery("SELECT cad FROM ConfigurationAuthenticationDetails AS cad where cad.id = :id")
        .setParameter("id", configuration.getId())
        .getSingleResult())
        .isNotNull();
  }

  @Test
  public void shouldDeleteWithCredentials() {
    // given
    Configuration configuration = new ConfigurationDataBuilder()
        .withCredentials(UUID.randomUUID().toString())
        .buildAsNew();

    repository.save(configuration);

    // when
    repository.delete(configuration.getId());

    // then
    assertThat(repository.exists(configuration.getId()))
        .isFalse();

    assertThatThrownBy(() -> entityManager
        .createQuery("SELECT cad FROM ConfigurationAuthenticationDetails AS cad where cad.id = :id")
        .setParameter("id", configuration.getId())
        .getSingleResult())
        .isInstanceOf(NoResultException.class);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowToHaveSeveralConfigurationsWithSameName() {
    repository.save(new ConfigurationDataBuilder().withName("test").buildAsNew());
    repository.save(new ConfigurationDataBuilder().withName("TEST").buildAsNew());
    repository.saveAndFlush(new ConfigurationDataBuilder().withName("TeSt").buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowToHaveSeveralConfigurationsWithSameTargetUrl() {
    repository.save(new ConfigurationDataBuilder().withTargetUrl("http://localhost").buildAsNew());
    repository.save(new ConfigurationDataBuilder().withTargetUrl("http://LOCALHOST").buildAsNew());
    repository.saveAndFlush(new ConfigurationDataBuilder().withTargetUrl("http://lOcAlHoSt").buildAsNew());
  }
}

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

package org.openlmis.integration.pcmt.service.fhir;

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class MeasureFhirServiceTest extends BaseFhirServiceTest<Measure> {

  @Mock
  private Measure measure1;

  @Mock
  private Measure measure2;

  private MeasureFhirService service;

  @Override
  BaseFhirService<Measure> getService() {
    return new MeasureFhirService();
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    service = (MeasureFhirService) prepareService();
  }

  @Test
  public void shouldFindMeasuresByNames() {
    // given
    List<String> names = Lists.newArrayList("one", "two");

    Bundle first = createBundle(measure1);
    Bundle second = createBundle(measure2);

    ICriterion<StringClientParam> where = Measure.NAME.matchesExactly().values(names);

    mockSearch(first, where);
    mockPages(first, second);

    // when
    Set<Measure> measures = service.getMeasures(names);

    assertThat(measures).containsExactlyInAnyOrder(measure1, measure2);
  }
}

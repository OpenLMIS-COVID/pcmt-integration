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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class LocationFhirServiceTest extends BaseFhirServiceTest<Location> {

  private static final String LOCATION_ID = "location/c71b0bcb-f736-4cc1-8272-511ce64e21e0";
  private static final String IDENTIFIER_SYSTEM = "http://localhost";
  private static final String IDENTIFIER_VALUE = "value";

  @Mock
  private Location location;

  private LocationFhirService service;

  @Override
  BaseFhirService<Location> getService() {
    return new LocationFhirService();
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    service = (LocationFhirService) prepareService();
  }

  @Test
  public void shouldFindLocationById() {
    // given
    mockRead(LOCATION_ID, location);

    // when
    Location found = service.getLocation(LOCATION_ID);

    // given
    assertThat(found).isEqualTo(location);
  }

  @Test
  public void shouldFindLocationByIdentifier() {
    // given
    Bundle bundle = createBundle(location);

    ICriterion<?> where = Location
        .IDENTIFIER
        .exactly()
        .systemAndValues(IDENTIFIER_SYSTEM, IDENTIFIER_VALUE);

    mockSearch(bundle, where);

    // when
    Location found = service.findByIdentifier(IDENTIFIER_SYSTEM, IDENTIFIER_VALUE);

    // given
    assertThat(found).isEqualTo(location);
  }
}

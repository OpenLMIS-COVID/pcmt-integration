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

package org.openlmis.integration.dhis2.service.fhir;

import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LocationFhirService extends BaseFhirService<Location> {

  public LocationFhirService() {
    super(Location.class);
  }

  /**
   * Find a location with id.
   */
  public Location getLocation(String id) {
    log().debug("Get location with id {}", id);
    Location resource = getResource(id);

    if (null == resource) {
      log().warn("Can't find location with id {}", id);
    } else {
      log().debug("Found location with id {}", id);
    }

    return resource;
  }

  /**
   * Finds location by identifier.
   */
  public Location findByIdentifier(String system, String value) {
    log().debug("Try to find location with identifier with system {} and value {}", system, value);
    Bundle bundle = searchResources()
        .where(Location.IDENTIFIER.exactly().systemAndValues(system, value))
        .execute();

    List<BundleEntryComponent> entries = bundle.getEntry();

    if (CollectionUtils.isEmpty(entries)) {
      log().warn("Can't find location with identifier with system {} and value {}", system, value);
      return null;
    }

    log().debug("Found location with identifier with system {} and value {}", system, value);
    return (Location) entries.get(0).getResource();
  }

}

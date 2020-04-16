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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Service;

@Service
public class MeasureFhirService extends BaseFhirService<Measure> {

  public MeasureFhirService() {
    super(Measure.class);
  }

  /**
   * Retrieve measures for given names.
   */
  public Set<Measure> getMeasures(Collection<String> names) {
    log().debug("Try to find measures with names {}", names);
    Bundle bundle = searchResources()
        .where(Measure.NAME.matchesExactly().values(Lists.newArrayList(names)))
        .execute();

    Set<Measure> measures = Sets.newHashSet();
    forEachBundle(bundle, page -> measures.addAll(getMeasures(page)));

    log().debug("Found {} measures for names {}", measures.size(), names);
    return measures;
  }

  private Set<Measure> getMeasures(Bundle bundle) {
    if (!bundle.hasEntry()) {
      return Collections.emptySet();
    }

    return bundle
        .getEntry()
        .stream()
        .filter(BundleEntryComponent::hasResource)
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource instanceof Measure)
        .map(resource -> (Measure) resource)
        .collect(Collectors.toSet());
  }
}

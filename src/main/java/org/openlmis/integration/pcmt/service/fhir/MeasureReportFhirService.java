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

import ca.uhn.fhir.rest.gclient.IQuery;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.springframework.stereotype.Service;

@Service
public class MeasureReportFhirService extends BaseFhirService<MeasureReport> {

  public MeasureReportFhirService() {
    super(MeasureReport.class);
  }

  /**
   * Retrieve measure reports for the given measures, only in the given period of time, and
   * (optionally) only for the given location.
   */
  public Set<MeasureReport> getMeasureReports(Collection<Measure> measures,
      LocalDate startDate, LocalDate endDate, String locationId) {
    Set<String> measuresById = measures
        .stream()
        .map(item -> item.getIdElement().getIdPart())
        .collect(Collectors.toSet());

    log().debug(
        "Try to find measure reports for measures {}, period {} {} and location {}",
        measuresById, startDate, endDate, locationId);

    IQuery<Bundle> query = searchResources()
        .where(MeasureReport
            .PERIOD
            .afterOrEquals()
            .day(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
        .and(MeasureReport
            .PERIOD
            .beforeOrEquals()
            .day(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
        .and(MeasureReport
            .MEASURE
            .hasAnyOfIds(measuresById));

    if (null != locationId) {
      query = query
          .and(MeasureReport
              .REPORTER
              .hasId(locationId));
    }

    Bundle bundle = query.execute();

    Set<MeasureReport> reports = Sets.newHashSet();
    forEachBundle(bundle, page -> reports.addAll(getReports(page)));

    log().debug("Found {} measure reports for measures {}, period {} {} and location {}",
        reports.size(), measuresById, startDate, endDate, locationId);
    return reports;
  }

  private Set<MeasureReport> getReports(Bundle bundle) {
    if (!bundle.hasEntry()) {
      return Collections.emptySet();
    }

    return bundle
        .getEntry()
        .stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource instanceof MeasureReport)
        .map(resource -> (MeasureReport) resource)
        .collect(Collectors.toSet());
  }

}

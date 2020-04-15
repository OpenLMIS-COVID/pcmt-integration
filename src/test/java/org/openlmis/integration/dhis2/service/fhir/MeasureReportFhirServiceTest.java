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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import ca.uhn.fhir.rest.gclient.DateClientParam.IDateCriterion;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class MeasureReportFhirServiceTest extends BaseFhirServiceTest<MeasureReport> {

  private static final String MEASURE_ID = "Measure/ac489fbe-a590-4fcd-b664-06f5a94ff3ec";
  private static final String LOCATION_ID = "Location/9c35c3b5-9b09-4032-8121-0e8aed012e36";

  private static final LocalDate START_DATE = LocalDate.of(2019, 5, 1);
  private static final LocalDate END_DATE = LocalDate.of(2019, 5, 31);

  @Mock
  private Measure measure;

  @Mock
  private MeasureReport report1;

  @Mock
  private MeasureReport report2;

  private MeasureReportFhirService service;

  @Override
  BaseFhirService<MeasureReport> getService() {
    return new MeasureReportFhirService();
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    service = (MeasureReportFhirService) prepareService();

    given(measure.getIdElement()).willReturn(new IdType(MEASURE_ID));
  }

  @Test
  public void shouldFindMeasureReportsByMeasuresAndPeriodRange() {
    // given
    Collection<Measure> measures = Lists.newArrayList(measure);

    Bundle first = createBundle(report1);
    Bundle second = createBundle(report2);

    IDateCriterion where = MeasureReport
        .PERIOD
        .afterOrEquals()
        .day(START_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));

    IDateCriterion and1 = MeasureReport
        .PERIOD
        .beforeOrEquals()
        .day(END_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));

    ICriterion<ReferenceClientParam> and2 = MeasureReport
        .MEASURE
        .hasAnyOfIds(Sets.newHashSet(MEASURE_ID));

    mockSearch(first, where, and1, and2);
    mockPages(first, second);

    // when
    Set<MeasureReport> reports = service
        .getMeasureReports(measures, START_DATE, END_DATE, null);

    assertThat(reports).containsExactlyInAnyOrder(report1, report2);
  }

  @Test
  public void shouldFindMeasureReportsByMeasuresAndPeriodRangeAndLocation() {
    // given
    Collection<Measure> measures = Lists.newArrayList(measure);

    Bundle first = createBundle(report1);
    Bundle second = createBundle(report2);

    IDateCriterion where = MeasureReport
        .PERIOD
        .afterOrEquals()
        .day(START_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));

    IDateCriterion and1 = MeasureReport
        .PERIOD
        .beforeOrEquals()
        .day(END_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE));

    ICriterion<ReferenceClientParam> and2 = MeasureReport
        .MEASURE
        .hasAnyOfIds(Sets.newHashSet(MEASURE_ID));

    ICriterion<ReferenceClientParam> and3 = MeasureReport
        .REPORTER
        .hasId(LOCATION_ID);

    mockSearch(first, where, and1, and2, and3);
    mockPages(first, second);

    // when
    Set<MeasureReport> reports = service
        .getMeasureReports(measures, START_DATE, END_DATE, LOCATION_ID);

    assertThat(reports).containsExactlyInAnyOrder(report1, report2);
  }
}

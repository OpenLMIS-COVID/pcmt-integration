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

package org.openlmis.integration.pcmt.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.openlmis.integration.pcmt.Dhis2Configuration;
import org.openlmis.integration.pcmt.service.fhir.LocationFhirService;
import org.openlmis.integration.pcmt.service.fhir.MeasureFhirService;
import org.openlmis.integration.pcmt.service.fhir.MeasureReportFhirService;
import org.openlmis.integration.pcmt.service.referencedata.FacilityDto;
import org.openlmis.integration.pcmt.service.referencedata.FacilityReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class PayloadBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(PayloadBuilder.class);
  private static final XLogger X_LOGGER = XLoggerFactory.getXLogger(PayloadBuilder.class);

  @Autowired
  private Dhis2Configuration dhis2Configuration;

  @Autowired
  private MeasureFhirService measureFhirService;

  @Autowired
  private MeasureReportFhirService measureReportFhirService;

  @Autowired
  private LocationFhirService locationFhirService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Value("${service.url}")
  private String serviceUrl;

  Payload build(LocalDate startDate, LocalDate endDate, String programName, UUID facilityId) {
    X_LOGGER.entry(startDate, endDate, programName, facilityId);

    Profiler profiler = new Profiler("BUILD_PAYLOAD");
    profiler.setLogger(X_LOGGER);

    profiler.start("GET_MEASURES");
    Map<String, Measure> measures = getMeasures();

    profiler.start("GET_MEASURE_REPORTS");
    Set<MeasureReport> measureReports = getMeasureReports(
        measures.values(), startDate, endDate, facilityId, programName);

    profiler.start("GET_FACILITIES");
    Map<String, FacilityDto> facilities = getFacilities(measureReports);

    profiler.start("GROUP_REPORTS_BY_REPORTER");
    Map<String, List<MeasureReport>> reportsPerFacility = measureReports
        .stream()
        .collect(Collectors.groupingBy(report -> report.getReporter().getReference()));

    profiler.start("CREATE_PAYLOAD_PER_FACILITY");
    Set<PayloadFacility> payloadFacilities = createPayloadPerFacility(
        measures, reportsPerFacility, facilities);

    profiler.start("INIT_PAYLOAD");
    Payload payload = new Payload(payloadFacilities, startDate);

    profiler.stop().log();
    X_LOGGER.exit(payload);

    return payload;
  }

  private Map<String, Measure> getMeasures() {
    return measureFhirService
        .getMeasures(dhis2Configuration.getMeasureCodes())
        .stream()
        .collect(Collectors.toMap(item -> item.getIdElement().getIdPart(), Function.identity()));
  }

  private Set<MeasureReport> getMeasureReports(Collection<Measure> measures, LocalDate startDate,
      LocalDate endDate, UUID facilityId, String programName) {
    String locationId = null;

    if (null != facilityId) {
      locationId = locationFhirService
          .findByIdentifier(serviceUrl, facilityId.toString())
          .getIdElement()
          .getIdPart();
    }

    return measureReportFhirService
        .getMeasureReports(measures, startDate, endDate, locationId)
        .stream()
        .filter(MeasureReport::hasGroup)
        .filter(report -> null == programName || matchProgram(report, programName))
        .collect(Collectors.toSet());
  }

  private boolean matchProgram(MeasureReport report, String programName) {
    LOGGER.info("Checking if report {} is for program {}", report.getId(), programName);
    boolean result = report
        .getGroup()
        .stream()
        .filter(item -> item.hasCode())
        .filter(item -> item.getCode().hasText())
        .filter(item -> item.hasMeasureScore())
        .filter(item -> item.getMeasureScore().hasSystem())
        .filter(item -> item.getMeasureScore().hasCode())
        .filter(item -> dhis2Configuration.getProgramNameCodeText()
            .equalsIgnoreCase(item.getCode().getText()))
        .filter(item -> dhis2Configuration.getMeasureScoreSystem()
            .equalsIgnoreCase(item.getMeasureScore().getSystem()))
        .allMatch(item -> item.getMeasureScore().getCode().equalsIgnoreCase(programName));

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "The report {} {} for program {}",
          report.getId(), result ? "is" : "is not", programName);
    }

    return result;
  }

  private Map<String, FacilityDto> getFacilities(Set<MeasureReport> measureReports) {
    Map<String, UUID> facilityIds = getFacilityIdsFromReports(measureReports);
    Map<UUID, FacilityDto> facilities = getActualFacilities(facilityIds);

    Map<String, FacilityDto> result = Maps.newHashMap();
    facilityIds.forEach((key, value) -> result.put(key, facilities.get(value)));

    return result;
  }

  private Map<String, UUID> getFacilityIdsFromReports(Set<MeasureReport> measureReports) {
    Map<String, UUID> facilityIds = Maps.newHashMap();
    for (MeasureReport measureReport : measureReports) {
      String reference = measureReport.getReporter().getReference();

      if (facilityIds.containsKey(reference)) {
        continue;
      }

      Location location = locationFhirService.getLocation(reference);
      location.getIdentifier()
          .stream()
          .filter(item -> item.hasSystem())
          .filter(item -> item.hasValue())
          .filter(item -> item.getSystem().equalsIgnoreCase(serviceUrl))
          .findFirst()
          .ifPresent(item -> facilityIds.put(reference, UUID.fromString(item.getValue())));
    }

    return facilityIds;
  }

  private Map<UUID, FacilityDto> getActualFacilities(Map<String, UUID> facilityIds) {
    return facilityReferenceDataService
        .search(facilityIds.values())
        .stream()
        .collect(Collectors.toMap(FacilityDto::getId, Function.identity()));
  }

  private Set<PayloadFacility> createPayloadPerFacility(Map<String, Measure> measures,
      Map<String, List<MeasureReport>> reportsPerFacility, Map<String, FacilityDto> facilities) {
    Set<PayloadFacility> payloadFacilities = Sets.newHashSet();

    for (Entry<String, List<MeasureReport>> entry : reportsPerFacility.entrySet()) {
      String facilityCode = facilities.get(entry.getKey()).getCode();
      LOGGER.debug("Creating payload for facility {}", facilityCode);
      Set<PayloadFacilityValue> values = createValues(entry.getValue(), measures);

      payloadFacilities.add(new PayloadFacility(facilityCode, values));
      LOGGER.debug("Created payload for facility {}", facilityCode);
    }

    return payloadFacilities;
  }

  private Set<PayloadFacilityValue> createValues(List<MeasureReport> reports,
      Map<String, Measure> measures) {
    Set<PayloadFacilityValue> values = Sets.newHashSet();

    for (MeasureReport report : reports) {
      String measureId = new IdType(report.getMeasure()).getIdPart();
      Measure measure = measures.get(measureId);
      String suffix = dhis2Configuration.getMeasureMapping(measure.getName());

      LOGGER.debug("Creating product values for measure {}", measureId);
      values.addAll(getProductValues(report, suffix));
      LOGGER.debug("Created product values for measure {}", measureId);
    }

    return values;
  }

  private Set<PayloadFacilityValue> getProductValues(MeasureReport report, String suffix) {
    return report
        .getGroup()
        .stream()
        .filter(item -> item.hasCode())
        .filter(item -> item.getCode().hasText())
        .filter(item -> item.hasMeasureScore())
        .filter(item -> !item.getMeasureScore().hasSystem())
        .filter(item -> !item.getMeasureScore().hasCode())
        .filter(item -> item.getMeasureScore().hasValue())
        .map(item -> new PayloadFacilityValue(
            item.getCode().getText() + "-" + suffix,
            item.getMeasureScore().getValue()))
        .collect(Collectors.toSet());
  }

}

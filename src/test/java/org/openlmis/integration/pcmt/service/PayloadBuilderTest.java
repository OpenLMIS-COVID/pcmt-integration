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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;

import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.assertj.core.util.Lists;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.integration.pcmt.Dhis2Configuration;
import org.openlmis.integration.pcmt.ObjectGenerator;
import org.openlmis.integration.pcmt.service.fhir.LocationFhirService;
import org.openlmis.integration.pcmt.service.fhir.MeasureFhirService;
import org.openlmis.integration.pcmt.service.fhir.MeasureReportFhirService;
import org.openlmis.integration.pcmt.service.referencedata.FacilityDto;
import org.openlmis.integration.pcmt.service.referencedata.FacilityReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("PMD.TooManyMethods")
public class PayloadBuilderTest {

  private static final String SERVICE_URL = "http://localhost";

  private static final LocalDate START_DATE = LocalDate.of(2019, 5, 1);
  private static final LocalDate END_DATE = LocalDate.of(2019, 5, 31);

  private static final String PROGRAM = "program";
  private static final String ANOTHER_PROGRAM = "anotherProgram";

  private static final String PRODUCT_CODE = "productCode";
  private static final Long PRODUCT_VALUE = 1000L;

  private static final String ANOTHER_PRODUCT_CODE = "anotherProductCode";
  private static final Long ANOTHER_PRODUCT_VALUE = 10L;

  private static final String MEASURE_CODE_1 = "measure1";
  private static final String MEASURE_CODE_2 = "measure2";

  private static final Set<String> MEASURE_CODES = Sets.newHashSet(MEASURE_CODE_1, MEASURE_CODE_2);

  private static final String MEASURE_SUFFIX_1 = "a";
  private static final String MEASURE_SUFFIX_2 = "b";

  private static final String programNameCodeText = "programName";
  private static final String measureScoreSystem = "openlmisProgramName";

  private static final String DESCRIPTION = "Stock indicators for May 2019 period";
  private static final String REPORTING_PERIOD = "201905";

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Dhis2Configuration dhis2Configuration;

  @Mock
  private MeasureFhirService measureFhirService;

  @Mock
  private MeasureReportFhirService measureReportFhirService;

  @Mock
  private LocationFhirService locationFhirService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  private PayloadBuilder builder;

  private List<FacilityDto> facilities;

  @Before
  public void setUp() {
    prepareFhirResources();

    ReflectionTestUtils.setField(builder, "serviceUrl", SERVICE_URL);

    given(dhis2Configuration.getMeasureCodes()).willReturn(MEASURE_CODES);

    given(dhis2Configuration.getMeasureMapping(MEASURE_CODE_1)).willReturn(MEASURE_SUFFIX_1);
    given(dhis2Configuration.getMeasureMapping(MEASURE_CODE_2)).willReturn(MEASURE_SUFFIX_2);

    given(dhis2Configuration.getProgramNameCodeText()).willReturn(programNameCodeText);
    given(dhis2Configuration.getMeasureScoreSystem()).willReturn(measureScoreSystem);
  }

  @Test
  public void shouldBuildPayload() {
    // when
    Payload payload = builder.build(START_DATE, END_DATE, null, null);

    // then
    assertThat(payload.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(payload.getReportingPeriod()).isEqualTo(REPORTING_PERIOD);

    Set<PayloadFacility> allFacilityData = payload.getFacilities();
    assertThat(allFacilityData).isNotEmpty();

    Map<String, Set<PayloadFacilityValue>> facilityData = allFacilityData
        .stream()
        .collect(Collectors.toMap(PayloadFacility::getFacilityCode, PayloadFacility::getValues));

    assertThat(facilityData)
        .containsKeys(facilities.get(0).getCode(), facilities.get(1).getCode())
        .doesNotContainKeys(facilities.get(2).getCode());

    assertPayloadFacility(facilityData.get(facilities.get(0).getCode()), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_1, ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE)
    ));

    assertPayloadFacility(facilityData.get(facilities.get(1).getCode()), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_1, ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_2, PRODUCT_CODE, PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_2, ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE)
    ));
  }

  @Test
  public void shouldBuildPayloadForSingleProgram() {
    // when
    Payload payload = builder.build(START_DATE, END_DATE, PROGRAM, null);

    // then
    assertThat(payload.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(payload.getReportingPeriod()).isEqualTo(REPORTING_PERIOD);

    Set<PayloadFacility> allFacilityData = payload.getFacilities();
    assertThat(allFacilityData).isNotEmpty();

    Map<String, Set<PayloadFacilityValue>> facilityData = allFacilityData
        .stream()
        .collect(Collectors.toMap(PayloadFacility::getFacilityCode, PayloadFacility::getValues));

    assertThat(facilityData)
        .containsKeys(facilities.get(0).getCode(), facilities.get(1).getCode())
        .doesNotContainKeys(facilities.get(2).getCode());

    assertPayloadFacility(facilityData.get(facilities.get(0).getCode()), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE)
    ));

    assertPayloadFacility(facilityData.get(facilities.get(1).getCode()), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_2, PRODUCT_CODE, PRODUCT_VALUE)
    ));
  }

  @Test
  public void shouldBuildPayloadForSingleFacility() {
    // given
    FacilityDto facility = facilities.get(0);
    UUID facilityId = facility.getId();
    String facilityCode = facility.getCode();

    // when
    Payload payload = builder.build(START_DATE, END_DATE, null, facilityId);

    // then
    assertThat(payload.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(payload.getReportingPeriod()).isEqualTo(REPORTING_PERIOD);

    Set<PayloadFacility> allFacilityData = payload.getFacilities();
    assertThat(allFacilityData)
        .isNotEmpty()
        .hasSize(1);

    Map<String, Set<PayloadFacilityValue>> facilityData = allFacilityData
        .stream()
        .collect(Collectors.toMap(PayloadFacility::getFacilityCode, PayloadFacility::getValues));

    assertThat(facilityData).containsKey(facilityCode);
    assertPayloadFacility(facilityData.get(facilityCode), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE),
        new ProductDetails(MEASURE_SUFFIX_1, ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE)
    ));
  }

  @Test
  public void shouldBuildPayloadForSingleProgramAndFacility() {
    // given
    FacilityDto facility = facilities.get(0);
    UUID facilityId = facility.getId();
    String facilityCode = facility.getCode();

    // when
    Payload payload = builder.build(START_DATE, END_DATE, PROGRAM, facilityId);

    // then
    assertThat(payload.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(payload.getReportingPeriod()).isEqualTo(REPORTING_PERIOD);

    Set<PayloadFacility> allFacilityData = payload.getFacilities();
    assertThat(allFacilityData)
        .isNotEmpty()
        .hasSize(1);

    Map<String, Set<PayloadFacilityValue>> facilityData = allFacilityData
        .stream()
        .collect(Collectors.toMap(PayloadFacility::getFacilityCode, PayloadFacility::getValues));

    assertThat(facilityData).containsKey(facilityCode);
    assertPayloadFacility(facilityData.get(facilityCode), Lists.newArrayList(
        new ProductDetails(MEASURE_SUFFIX_1, PRODUCT_CODE, PRODUCT_VALUE)
    ));
  }

  private void assertPayloadFacility(Set<PayloadFacilityValue> values,
      List<ProductDetails> productDetails) {
    assertThat(values).hasSize(productDetails.size());

    for (int i = productDetails.size() - 1; i >= 0; --i) {
      ProductDetails product = productDetails.get(i);

      for (PayloadFacilityValue value : values) {
        if (product.match(value)) {
          productDetails.remove(i);
        }
      }
    }

    assertThat(productDetails).as("Can't match products: " + productDetails).isEmpty();
  }

  private void prepareFhirResources() {
    Measure measure1 = createMeasure(MEASURE_CODE_1);
    Measure measure2 = createMeasure(MEASURE_CODE_2);
    given(measureFhirService.getMeasures(MEASURE_CODES))
        .willReturn(Sets.newHashSet(measure1, measure2));

    facilities = ObjectGenerator.of(FacilityDto.class, 3);
    given(facilityReferenceDataService.search(anyCollectionOf(UUID.class)))
        .willReturn(facilities);

    Location location1 = createLocation(facilities.get(0));
    Location location2 = createLocation(facilities.get(1));
    Location location3 = createLocation(facilities.get(2));

    MeasureReport report1 = createMeasureReport(measure1, location1, PROGRAM, PRODUCT_CODE,
        PRODUCT_VALUE);
    MeasureReport report2 = createMeasureReport(measure1, location1, ANOTHER_PROGRAM,
        ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE);

    MeasureReport report3 = createMeasureReport(measure1, location2, PROGRAM, PRODUCT_CODE,
        PRODUCT_VALUE);
    MeasureReport report4 = createMeasureReport(measure1, location2, ANOTHER_PROGRAM,
        ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE);

    MeasureReport report5 = createMeasureReport(measure2, location2, PROGRAM, PRODUCT_CODE,
        PRODUCT_VALUE);
    MeasureReport report6 = createMeasureReport(measure2, location2, ANOTHER_PROGRAM,
        ANOTHER_PRODUCT_CODE, ANOTHER_PRODUCT_VALUE);

    given(measureReportFhirService
        .getMeasureReports(anySetOf(Measure.class), eq(START_DATE), eq(END_DATE), eq(null)))
        .willReturn(Sets.newHashSet(report1, report2, report3, report4, report5, report6));

    given(measureReportFhirService
        .getMeasureReports(anySetOf(Measure.class), eq(START_DATE), eq(END_DATE),
            eq(location1.getIdElement().getIdPart())))
        .willReturn(Sets.newHashSet(report1, report2));

    given(measureReportFhirService
        .getMeasureReports(anySetOf(Measure.class), eq(START_DATE), eq(END_DATE),
            eq(location2.getIdElement().getIdPart())))
        .willReturn(Sets.newHashSet(report3, report4, report5, report6));

    given(measureReportFhirService
        .getMeasureReports(anySetOf(Measure.class), eq(START_DATE), eq(END_DATE),
            eq(location3.getIdElement().getIdPart())))
        .willReturn(Sets.newHashSet());
  }

  private Measure createMeasure(String measureCode) {
    Measure measure = new Measure();
    measure.setId(new IdType(Measure.class.getSimpleName(), UUID.randomUUID().toString()));
    measure.setName(measureCode);
    return measure;
  }

  private Location createLocation(FacilityDto facility) {
    List<Identifier> identifiers = Lists.newArrayList();

    // location has to have at least one identifier
    // which has to point to facility in the reference data service
    int numberOfIdentifiers = RandomUtils.nextInt(1, 3);
    for (int i = 0; i < numberOfIdentifiers; ++i) {
      Identifier identifier = new Identifier();
      identifier.setSystem(i == 0 ? SERVICE_URL : RandomStringUtils.randomAlphabetic(5));
      identifier.setValue(i == 0 ? facility.getId().toString() : UUID.randomUUID().toString());

      identifiers.add(identifier);
    }

    Location location = new Location();
    location.setId(new IdType(Location.class.getSimpleName(), UUID.randomUUID().toString()));
    location.setIdentifier(identifiers);

    given(locationFhirService.getLocation(location.getId())).willReturn(location);
    given(locationFhirService.findByIdentifier(SERVICE_URL, facility.getId().toString()))
        .willReturn(location);

    return location;
  }

  private MeasureReport createMeasureReport(Measure measure, Location location, String programName,
      String productCode, Long productValue) {
    CodeableConcept programGroupCode = new CodeableConcept();
    programGroupCode.setText(programNameCodeText);

    Quantity programScore = new Quantity();
    programScore.setSystem(measureScoreSystem);
    programScore.setCode(programName);

    MeasureReportGroupComponent programGroup = new MeasureReportGroupComponent();
    programGroup.setCode(programGroupCode);
    programGroup.setMeasureScore(programScore);

    CodeableConcept productGroupCode = new CodeableConcept();
    productGroupCode.setText(productCode);

    Quantity productScore = new Quantity();
    productScore.setValue(productValue);

    MeasureReportGroupComponent productGroup = new MeasureReportGroupComponent();
    productGroup.setCode(productGroupCode);
    productGroup.setMeasureScore(productScore);

    List<MeasureReportGroupComponent> groups = Lists.newArrayList();
    groups.add(programGroup);
    groups.add(productGroup);

    MeasureReport report = new MeasureReport();
    report.setMeasure(measure.getId());
    report.setReporter(new Reference(location.getId()));
    report.setGroup(groups);

    return report;
  }

  @RequiredArgsConstructor
  private static final class ProductDetails {

    private final String measureSuffix;
    private final String productCode;
    private final long productValue;

    boolean match(PayloadFacilityValue value) {
      return value.getProductCode().startsWith(productCode + "-")
          && value.getProductCode().endsWith(measureSuffix)
          && value.getValue().longValue() == productValue;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
          .append("measureSuffix", measureSuffix)
          .append("productCode", productCode)
          .append("productValue", productValue)
          .toString();
    }
  }

}


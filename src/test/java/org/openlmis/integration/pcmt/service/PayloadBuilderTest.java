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

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("PMD.TooManyMethods")
public class PayloadBuilderTest {

  private static final String SERVICE_URL = "http://localhost";

  private static final String DESCRIPTION = "Stock indicators for May 2019 period";
  private static final String REPORTING_PERIOD = "201905";

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @InjectMocks
  private PayloadBuilder builder;


  @Before
  public void setUp() {

    ReflectionTestUtils.setField(builder, "serviceUrl", SERVICE_URL);

  }

  @Test
  @Ignore
  public void shouldBuildPayload() {
    // when
    Payload payload = builder.build(null);

    // then
    assertThat(payload.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(payload.getReportingPeriod()).isEqualTo(REPORTING_PERIOD);

    Set<PayloadFacility> allFacilityData = payload.getFacilities();
    assertThat(allFacilityData).isNotEmpty();

  }

}


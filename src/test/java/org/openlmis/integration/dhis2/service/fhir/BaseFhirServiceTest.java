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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IGetPage;
import ca.uhn.fhir.rest.gclient.IGetPageTyped;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IRead;
import ca.uhn.fhir.rest.gclient.IReadExecutable;
import ca.uhn.fhir.rest.gclient.IReadTyped;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import java.util.List;
import org.assertj.core.util.Lists;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.integration.dhis2.service.auth.AuthService;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class BaseFhirServiceTest<T extends IBaseResource> {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private AuthService authService;

  @Mock
  private IGenericClient client;

  @Mock
  private IRead read;

  @Mock
  private IReadTyped readTyped;

  @Mock
  private IReadExecutable readExecutable;

  @Mock
  private IUntypedQuery search;

  @Mock
  private IQuery query;

  @Mock
  private IGetPage loadPage;

  @Mock
  private IGetPageTyped loadPageTyped;

  @Mock
  private CacheControlDirective cacheControl;

  @Before
  public void setUp() {
    given(client.read()).willReturn(read);
    given(client.search()).willReturn(search);
    given(client.loadPage()).willReturn(loadPage);

    given(read.resource(any(Class.class))).willReturn(readTyped);
    given(search.forResource(any(Class.class))).willReturn(query);

    given(query.cacheControl(cacheControl)).willReturn(query);
    given(query.count(anyInt())).willReturn(query);
    given(query.returnBundle(Bundle.class)).willReturn(query);
  }

  BaseFhirService<T> prepareService() {
    BaseFhirService<T> service = getService();

    // mock properties from application.properties
    ReflectionTestUtils.setField(service, "fhirUrl", "http://localhost");
    ReflectionTestUtils.setField(service, "loggingEnable", true);
    ReflectionTestUtils.setField(service, "loggingVerbose", false);

    // mock external service
    ReflectionTestUtils.setField(service, "authService", authService);

    // mock fields that are generated in the afterPropertiesSet() method
    ReflectionTestUtils.setField(service, "client", client);
    ReflectionTestUtils.setField(service, "cacheControl", cacheControl);

    return service;
  }

  void mockRead(String id, T response) {
    given(readTyped.withId(id)).willReturn(readExecutable);
    given(readExecutable.execute()).willReturn(response);
  }

  void mockSearch(Bundle response, ICriterion where, ICriterion... and) {
    given(query.where(any(where.getClass()))).willReturn(query);

    for (ICriterion item : and) {
      given(query.and(any(item.getClass()))).willReturn(query);
    }

    given(query.execute()).willReturn(response);
  }

  void mockPages(Bundle first, Bundle second) {
    given(first.getLink(IBaseBundle.LINK_NEXT)).willReturn(mock(BundleLinkComponent.class));
    given(second.getLink(IBaseBundle.LINK_NEXT)).willReturn(null);


    given(loadPage.next(first)).willReturn(loadPageTyped);
    given(loadPageTyped.execute()).willReturn(second);
  }

  Bundle createBundle(Resource... resources) {
    Bundle bundle = mock(Bundle.class);
    List<BundleEntryComponent> entries = Lists.newArrayList();

    for (Resource resource : resources) {
      BundleEntryComponent entry = mock(BundleEntryComponent.class);
      given(entry.hasResource()).willReturn(true);
      given(entry.getResource()).willReturn(resource);

      entries.add(entry);
    }

    given(bundle.hasEntry()).willReturn(!entries.isEmpty());
    given(bundle.getEntry()).willReturn(entries);

    return bundle;
  }

  abstract BaseFhirService<T> getService();
}

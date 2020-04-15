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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.IQuery;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.openlmis.integration.dhis2.service.auth.AuthService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

abstract class BaseFhirService<T extends IBaseResource> implements InitializingBean {

  private static final int WAIT_TIME = (int) TimeUnit.MINUTES.toMillis(15);

  @Value("${fhir.url}")
  private String fhirUrl;

  @Value("${fhir.logging.enable}")
  private boolean loggingEnable;

  @Value("${fhir.logging.verbose}")
  private boolean loggingVerbose;

  @Autowired
  private AuthService authService;

  private IGenericClient client;
  private CacheControlDirective cacheControl;

  private Class<T> resourceClass;

  BaseFhirService(Class<T> resourceClass) {
    this.resourceClass = resourceClass;
  }

  @Override
  public void afterPropertiesSet() {
    FhirContext fhirContext = FhirContext.forR4();

    IRestfulClientFactory clientFactory = fhirContext.getRestfulClientFactory();
    clientFactory.setConnectTimeout(WAIT_TIME);
    clientFactory.setConnectionRequestTimeout(WAIT_TIME);
    clientFactory.setSocketTimeout(WAIT_TIME);

    client = clientFactory.newGenericClient(fhirUrl);

    if (loggingEnable) {
      client.registerInterceptor(new LoggingInterceptor(loggingVerbose));
    }

    client.registerInterceptor(new DynamicBearerTokenAuthInterceptor(authService));

    cacheControl = new CacheControlDirective();
    cacheControl.setNoCache(true);
  }

  T getResource(String id) {
    return client
        .read()
        .resource(resourceClass)
        .withId(id)
        .execute();
  }

  IQuery<Bundle> searchResources() {
    return client
        .search()
        .forResource(resourceClass)
        .cacheControl(cacheControl)
        .count(100)
        .returnBundle(Bundle.class);
  }

  void forEachBundle(Bundle bundle, Consumer<Bundle> action) {
    Bundle page = bundle;
    action.accept(page);

    while (null != page.getLink(IBaseBundle.LINK_NEXT)) {
      page = client.loadPage().next(page).execute();
      action.accept(page);
    }
  }

}

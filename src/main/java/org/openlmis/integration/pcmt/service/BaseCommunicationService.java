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

import static org.openlmis.integration.pcmt.service.RequestHelper.createUri;
import static org.openlmis.integration.pcmt.service.RequestHelper.splitRequest;

import com.google.common.collect.Lists;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.Setter;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

public abstract class BaseCommunicationService<T> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Setter
  @Autowired
  private AuthService authService;

  @Value("${request.maxUrlLength}")
  protected int maxUrlLength;

  @Setter
  @Autowired
  protected RestOperations restTemplate;

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  /**
   * Return one object from service.
   *
   * @param id UUID of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    RequestParameters parameters = RequestParameters.init();
    String url = getServiceUrl() + getUrl() + id.toString();

    try {
      ResponseEntity<T> responseEntity = restTemplate.exchange(
          createUri(url, parameters), HttpMethod.GET, createEntity(),
          getResultClass());
      return responseEntity.getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
        logger.warn(
            "{} matching params does not exist. Params: {}",
            getResultClass().getSimpleName(), parameters
        );

        return null;
      } else {
        throw buildDataRetrievalException(ex);
      }
    }
  }

  protected <P> P get(Class<P> type, String resourceUrl, RequestParameters parameters) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    HttpEntity<Object> entity = createEntity();
    URI uri = createUri(url, parameters);
    ResponseEntity<P> response = restTemplate.exchange(uri, HttpMethod.GET, entity, type);

    return response.getBody();
  }

  protected List<T> findAll(String resourceUrl, RequestParameters parameters) {
    return findAllWithMethod(resourceUrl, parameters);
  }

  private List<T> findAllWithMethod(String resourceUrl, RequestParameters uriParameters) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    try {
      ResponseEntity<T[]> responseEntity = doListRequest(
          url, uriParameters, getArrayResultClass());

      return Lists.newArrayList(Arrays.asList(responseEntity.getBody()));
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with GET request.
   *
   * @param parameters Map of query parameters.
   * @return Page of reference data T objects.
   */
  protected Page<T> getPage(RequestParameters parameters) {
    String url = getServiceUrl() + getUrl() + "";

    try {
      ResponseEntity<PageDto<T>> response = doPageRequest(url, parameters, getResultClass());
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  private  <E> ResponseEntity<E[]> doListRequest(String url, RequestParameters parameters,
      Class<E[]> type) {
    HttpEntity<Object> entity = RequestHelper
        .createEntity(RequestHeaders.init().setAuth(authService.obtainAccessToken()));
    List<E[]> arrays = new ArrayList<>();

    for (URI uri : splitRequest(url, parameters, maxUrlLength)) {
      arrays.add(restTemplate.exchange(uri, HttpMethod.GET, entity, type).getBody());
    }

    E[] body = Merger
        .ofArrays(arrays)
        .withDefaultValue(() -> (E[]) Array.newInstance(type.getComponentType(), 0))
        .merge();

    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private <E> ResponseEntity<PageDto<E>> doPageRequest(String url, RequestParameters parameters,
      Class<E> type) {
    HttpEntity<Object> entity = createEntity();
    ParameterizedTypeReference<PageDto<E>> parameterizedType =
        new DynamicPageTypeReference<>(type);
    List<PageDto<E>> pages = new ArrayList<>();

    for (URI uri : splitRequest(url, parameters, maxUrlLength)) {
      pages.add(restTemplate.exchange(uri, HttpMethod.GET, entity, parameterizedType).getBody());
    }

    PageDto<E> body = Merger
        .ofPages(pages)
        .withDefaultValue(PageDto::new)
        .merge();

    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  protected DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(getResultClass().getSimpleName(), ex);
  }

  private <E> HttpEntity<E> createEntity() {
    return RequestHelper.createEntity(createHeadersWithAuth());
  }

  protected RequestHeaders createHeadersWithAuth() {
    return RequestHeaders.init().setAuth(authService.obtainAccessToken());
  }

}

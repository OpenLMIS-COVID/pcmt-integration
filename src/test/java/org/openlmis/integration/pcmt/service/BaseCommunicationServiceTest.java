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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.integration.pcmt.service.auth.AuthService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationServiceTest<T> {

  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String TOKEN_HEADER = "Bearer " + TOKEN;

  private static final String URI_QUERY_NAME = "name";
  private static final String URI_QUERY_VALUE = "value";

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  protected RestTemplate restTemplate;

  @Mock
  private AuthService authService;

  @Captor
  protected ArgumentCaptor<URI> uriCaptor;

  @Captor
  private ArgumentCaptor<HttpMethod> methodCaptor;

  @Captor
  protected ArgumentCaptor<HttpEntity> entityCaptor;

  private BaseCommunicationService<T> service;

  @Before
  public void setUp() {
    mockAuth();
    service = prepareService();
  }

  @After
  public void tearDown() {
    checkAuth();
  }

  @Test
  public void shouldFindById() {
    // given
    UUID id = UUID.randomUUID();

    // when
    T instance = mockResponseEntityAndGetDto();
    T found = service.findOne(id);

    // then
    assertThat(found, is(instance));

    verifyRequest()
        .isGetRequest()
        .hasAuthHeader()
        .hasEmptyBody()
        .isUriStartsWith(service.getServiceUrl() + service.getUrl() + id);
  }

  @Test
  public void shouldReturnNullIfEntityCannotBeFoundById() {
    // given
    UUID id = UUID.randomUUID();

    // when
    mockRequestFail(HttpStatus.NOT_FOUND);

    T found = service.findOne(id);

    // then
    assertThat(found, is(nullValue()));

    verifyRequest()
        .isGetRequest()
        .hasAuthHeader()
        .hasEmptyBody()
        .isUriStartsWith(service.getServiceUrl() + service.getUrl() + id);
  }

  @Test
  public void shouldThrowExceptionIfThereIsOtherProblemWithFindingById() {
    // given
    UUID id = UUID.randomUUID();

    // when
    expectedException.expect(DataRetrievalException.class);
    mockRequestFail(HttpStatus.BAD_REQUEST);

    service.findOne(id);
  }

  protected abstract T generateInstance();

  protected abstract BaseCommunicationService<T> getService();

  protected BaseCommunicationService<T> prepareService() {
    BaseCommunicationService<T> service = getService();
    service.setRestTemplate(restTemplate);
    service.setAuthService(authService);

    ReflectionTestUtils.setField(service, "maxUrlLength", 2000);

    return service;
  }

  private void mockAuth() {
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
  }

  private void checkAuth() {
    verify(authService, atLeastOnce()).obtainAccessToken();
  }

  protected T mockResponseEntityAndGetDto() {
    T dto = generateInstance();
    mockResponseEntity(dto);
    return dto;
  }

  protected T mockArrayResponseEntityAndGetDto() {
    T dto = generateInstance();
    mockArrayResponseEntity(dto);
    return dto;
  }

  protected T mockPageResponseEntityAndGetDto() {
    T dto = generateInstance();
    mockPageResponseEntity(dto);
    return dto;
  }

  protected void mockResponseEntity(T dto) {
    ResponseEntity<T> response = mock(ResponseEntity.class);
    doReturn(dto).when(response).getBody();

    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
        eq(getService().getResultClass())))
        .thenReturn(response);

  }

  protected void mockArrayResponseEntity(T dto) {
    mockArrayResponseEntity(Collections.singletonList(dto).toArray());
  }

  protected <E> void mockArrayResponseEntity(E[] array) {
    ResponseEntity<E[]> response = mock(ResponseEntity.class);
    doReturn(array).when(response).getBody();

    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
        any(Class.class)))
        .thenAnswer(invocation ->
            ((Class) invocation.getArguments()[3]).isArray() ? response : null);
  }

  protected <E> void mockPageResponseEntity(E dto) {
    mockPageResponseEntity(ImmutableList.of(dto));
  }

  protected <E> void mockPageResponseEntity(List<E> list) {
    PageDto<E> page = new PageDto<>();
    page.setContent(list);

    ResponseEntity<PageDto<E>> response = mock(ResponseEntity.class);
    doReturn(page).when(response).getBody();

    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
        any(DynamicPageTypeReference.class)))
        .thenReturn(response);
  }

  protected <E> void mockResultResponseEntity(E result) {
    ResultDto<E> dto = new ResultDto<>();
    dto.setResult(result);

    ResponseEntity<ResultDto> response = mock(ResponseEntity.class);
    doReturn(dto).when(response).getBody();

    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
        any(Class.class))).thenReturn(response);
  }

  protected void mockRequestFail(HttpStatus statusCode) {
    mockRequestFail(new HttpClientErrorException(statusCode));
  }

  protected void mockRequestFail(Exception exception) {
    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class),
        any(HttpEntity.class), any(Class.class)))
        .thenThrow(exception);
  }

  protected RequestSummary verifyRequest() {
    verify(restTemplate, atLeastOnce()).exchange(
        uriCaptor.capture(), methodCaptor.capture(), entityCaptor.capture(),
        eq(getService().getResultClass())
    );

    return new RequestSummary(
        uriCaptor.getValue(), methodCaptor.getValue(), entityCaptor.getValue()
    );
  }

  protected RequestSummary verifyArrayRequest() {
    return verifyArrayRequest(getService().getArrayResultClass());
  }

  protected <E> RequestSummary verifyArrayRequest(Class<E[]> type) {
    verify(restTemplate, atLeastOnce()).exchange(
        uriCaptor.capture(), methodCaptor.capture(), entityCaptor.capture(),
        eq(type)
    );

    return new RequestSummary(
        uriCaptor.getValue(), methodCaptor.getValue(), entityCaptor.getValue()
    );
  }

  protected RequestSummary verifyPageRequest() {
    verify(restTemplate, atLeastOnce()).exchange(
        uriCaptor.capture(), methodCaptor.capture(), entityCaptor.capture(),
        any(DynamicPageTypeReference.class)
    );

    return new RequestSummary(
        uriCaptor.getValue(), methodCaptor.getValue(), entityCaptor.getValue()
    );
  }

  protected RequestSummary verifyResultRequest() {
    verify(restTemplate, atLeastOnce()).exchange(
        uriCaptor.capture(), methodCaptor.capture(), entityCaptor.capture(),
        any(Class.class)
    );

    return new RequestSummary(
        uriCaptor.getValue(), methodCaptor.getValue(), entityCaptor.getValue()
    );
  }

  protected static final class RequestSummary {

    private String uri;
    private List<NameValuePair> queryParams;

    private HttpMethod method;
    private HttpEntity entity;

    private RequestSummary(URI uri, HttpMethod method, HttpEntity entity) {
      this.uri = uri.toString();
      this.method = method;
      this.entity = entity;
      this.queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
    }

    public RequestSummary isGetRequest() {
      assertThat(method, is(HttpMethod.GET));
      return this;
    }

    public RequestSummary hasAuthHeader() {
      List<String> authorization = entity.getHeaders().get(HttpHeaders.AUTHORIZATION);

      assertThat(authorization, hasSize(1));
      assertThat(authorization, hasItem(TOKEN_HEADER));

      return this;
    }

    public RequestSummary hasEmptyBody() {
      assertThat(entity.getBody(), is(nullValue()));
      return this;
    }

    public RequestSummary isUriStartsWith(String prefix) {
      assertThat(uri, startsWith(prefix));
      return this;
    }

    public RequestSummary hasQueryParameter(String field, Object value) {
      if (null != value) {
        assertThat(queryParams, hasItem(allOf(
            hasProperty(URI_QUERY_NAME, is(field)),
            hasProperty(URI_QUERY_VALUE, is(value.toString())))
        ));
      } else {
        assertThat(queryParams, not(hasItem(hasProperty(URI_QUERY_NAME, is(field)))));
      }

      return this;
    }
  }

}

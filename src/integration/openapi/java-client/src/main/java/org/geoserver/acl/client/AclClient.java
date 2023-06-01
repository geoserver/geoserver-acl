/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.geoserver.acl.api.client.AdminRulesApi;
import org.geoserver.acl.api.client.ApiClient;
import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.client.RulesApi;
import org.geoserver.acl.api.client.auth.HttpBasicAuth;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AclClient {

    private ApiClient apiClient;

    public AclClient() {
        RestTemplate restTemplate = createRestTemplate();
        apiClient = new ApiClient(restTemplate);
    }

    public AclClient setBasePath(String basePath) {
        apiClient.setBasePath(basePath);
        return this;
    }

    public String getBasePath() {
        return apiClient.getBasePath();
    }

    public AclClient setUsername(String username) {
        apiClient.setUsername(username);
        return this;
    }

    public String getUsername() {
        HttpBasicAuth auth = getBasicAuth();
        return auth == null ? null : auth.getUsername();
    }

    public AclClient setPassword(String pwd) {
        apiClient.setPassword(pwd);
        return this;
    }

    public String getPassword() {
        HttpBasicAuth auth = getBasicAuth();
        return auth == null ? null : auth.getPassword();
    }

    public AclClient setLogRequests(boolean logRequests) {
        apiClient.setDebugging(logRequests);
        return this;
    }

    public boolean isLogRequests() {
        return apiClient.isDebugging();
    }

    private HttpBasicAuth getBasicAuth() {
        return apiClient.getAuthentications().values().stream()
                .filter(HttpBasicAuth.class::isInstance)
                .findFirst()
                .map(HttpBasicAuth.class::cast)
                .orElse(null);
    }

    public RulesApi getRulesApi() {
        return new RulesApi(apiClient);
    }

    public AdminRulesApi getAdminRulesApi() {
        return new AdminRulesApi(apiClient);
    }

    public AuthorizationApi getAuthorizationApi() {
        return new AuthorizationApi(apiClient);
    }

    static RestTemplate createRestTemplate() {

        // Use Apache HttpComponents HttpClient, otherwise
        // SimpleClientHttpRequestFactory fails on
        // PATCH requests
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        // This allows us to read the response more than once - Necessary for debugging
        restTemplate.setRequestFactory(
                new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));

        // disable default URL encoding
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        List<HttpMessageConverter<?>> messageConverters =
                restTemplate.getMessageConverters().stream()
                        .filter(m -> !(MappingJackson2HttpMessageConverter.class.isInstance(m)))
                        .collect(Collectors.toCollection(ArrayList::new));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        messageConverters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }
}

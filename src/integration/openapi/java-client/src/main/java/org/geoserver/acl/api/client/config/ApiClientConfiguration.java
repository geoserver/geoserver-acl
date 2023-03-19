/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.geoserver.acl.api.client.AdminRulesApi;
import org.geoserver.acl.api.client.ApiClient;
import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.client.RulesApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * Include this configuration to contribute an {@link org.geoserver.acl.api.client.ApiClient}
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class ApiClientConfiguration {

    @Bean
    ApiClient aclApiClient(
            ApiClientProperties config,
            @Qualifier("aclClientRestTemplate") RestTemplate restTemplate) {

        String basePath = config.getBasePath();
        String username = config.getUsername();
        String password = config.getPassword();
        boolean debugging = config.isDebug();

        ApiClient apiClient = new ApiClient(restTemplate);
        if (null == basePath) {
            throw new IllegalStateException(
                    "Authorization service target URL not provided through config property geoserver.acl.client.basePath");
        }
        apiClient.setBasePath(basePath);

        apiClient.setDebugging(debugging);
        apiClient.setUsername(username);
        apiClient.setPassword(password);
        return apiClient;
    }

    @Bean
    RulesApi aclRulesApiClient(ApiClient client) {
        return new RulesApi(client);
    }

    @Bean
    AdminRulesApi aclAdminRulesApiClient(ApiClient client) {
        return new AdminRulesApi(client);
    }

    @Bean
    AuthorizationApi aclAuthorizationApiClient(ApiClient client) {
        return new AuthorizationApi(client);
    }

    @Bean
    RestTemplate aclClientRestTemplate(
            @Qualifier("aclClientObjectMapper") ObjectMapper objectMapper) {

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

        messageConverters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    @Bean
    ObjectMapper aclClientObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}

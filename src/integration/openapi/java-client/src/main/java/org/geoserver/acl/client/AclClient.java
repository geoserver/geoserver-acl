/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.geoserver.acl.api.client.ApiClient;
import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.client.DataRulesApi;
import org.geoserver.acl.api.client.WorkspaceAdminRulesApi;
import org.geoserver.acl.api.client.auth.HttpBasicAuth;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

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

    public DataRulesApi getRulesApi() {
        return new DataRulesApi(apiClient);
    }

    public WorkspaceAdminRulesApi getAdminRulesApi() {
        return new WorkspaceAdminRulesApi(apiClient);
    }

    public AuthorizationApi getAuthorizationApi() {
        return new AuthorizationApi(apiClient);
    }

    static RestTemplate createRestTemplate() {

        // Use Apache HttpComponents HttpClient, otherwise SimpleClientHttpRequestFactory fails on
        // PATCH requests
        // ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        ClientHttpRequestFactory requestFactory = getClientHttpRequestFactoryForHttps();
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // This allows us to read the response more than once - Necessary for debugging
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));

        // disable default URL encoding
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters().stream()
                .filter(m -> !(m instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toCollection(ArrayList::new));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        messageConverters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    static ClientHttpRequestFactory getClientHttpRequestFactoryForHttps() {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient =
                HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}

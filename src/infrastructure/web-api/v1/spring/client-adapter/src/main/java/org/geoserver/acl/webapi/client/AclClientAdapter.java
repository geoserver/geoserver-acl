/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.webapi.v1.client.ApiClient;
import org.geoserver.acl.webapi.v1.client.AuthorizationApi;
import org.geoserver.acl.webapi.v1.client.DataRulesApi;
import org.geoserver.acl.webapi.v1.client.WorkspaceAdminRulesApi;
import org.geoserver.acl.webapi.v1.client.auth.HttpBasicAuth;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/**
 * Client for the GeoServer ACL (Access Control List) REST API.
 * <p>
 * This client provides access to the ACL API endpoints for managing data rules,
 * workspace admin rules, and authorization queries.
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * AclClientAdapter client = new AclClientAdapter()
 *     .setBasePath("http://localhost:8080/api")
 *     .setUsername("admin")
 *     .setPassword("password");
 *
 * RuleAdminService rulesService = client.createRuleAdminService();
 * rulesService.getAll().forEach(System.out::println);
 *
 * AdminRuleAdminService adminRulesService = client.createAdminRuleAdminService();
 * adminRulesService.getAll().forEach(System.out::println);
 *
 * AuthorizationService authService = client.createAuthorizationService();
 * AccessRequest req = AccessRequest.builder()....build();
 * AccessInfo grants = authorizationService.getAccessInfo(req);
 * }
 * </pre>
 */
public class AclClientAdapter {

    private final ApiClient apiClient;

    /**
     * Creates a new ACL client with default configuration.
     * <p>
     * The client is configured with HTTPS support and accepts all SSL certificates.
     * Use {@link #setBasePath(String)} to configure the API endpoint URL.
     * </p>
     */
    public AclClientAdapter() {
        RestClient restClient = createRestClient();
        this.apiClient = new ApiClient(restClient);
    }

    public RuleRepository createRuleRepository() {
        return new RuleRepositoryClientAdaptor(new DataRulesApi(apiClient));
    }

    public AdminRuleRepository createAdminRuleRepository() {
        return new AdminRuleRepositoryClientAdaptor(new WorkspaceAdminRulesApi(apiClient));
    }

    public RuleAdminService createRuleAdminService() {
        return new RuleAdminServiceImpl(createRuleRepository());
    }

    public AdminRuleAdminService createAdminRuleAdminService() {
        return new AdminRuleAdminServiceImpl(createAdminRuleRepository());
    }

    public AuthorizationService createAuthorizationService() {
        return new AuthorizationServiceClientAdaptor(new AuthorizationApi(apiClient));
    }

    /**
     * Sets the base path for the ACL API.
     *
     * @param basePath the base URL of the ACL service (e.g., "http://localhost:8080/api")
     * @return this client instance for method chaining
     */
    public AclClientAdapter setBasePath(String basePath) {
        apiClient.setBasePath(basePath);
        return this;
    }

    /**
     * Gets the configured base path.
     *
     * @return the base URL of the ACL service
     */
    public String getBasePath() {
        return apiClient.getBasePath();
    }

    /**
     * Sets the username for HTTP Basic authentication.
     *
     * @param username the username
     * @return this client instance for method chaining
     */
    public AclClientAdapter setUsername(String username) {
        apiClient.setUsername(username);
        return this;
    }

    /**
     * Gets the configured username for HTTP Basic authentication.
     *
     * @return the username, or null if not configured
     */
    public String getUsername() {
        HttpBasicAuth auth = getBasicAuth();
        return auth == null ? null : auth.getUsername();
    }

    /**
     * Sets the password for HTTP Basic authentication.
     *
     * @param pwd the password
     * @return this client instance for method chaining
     */
    public AclClientAdapter setPassword(String pwd) {
        apiClient.setPassword(pwd);
        return this;
    }

    /**
     * Gets the configured password for HTTP Basic authentication.
     *
     * @return the password, or null if not configured
     */
    public String getPassword() {
        HttpBasicAuth auth = getBasicAuth();
        return auth == null ? null : auth.getPassword();
    }

    private HttpBasicAuth getBasicAuth() {
        return apiClient.getAuthentications().values().stream()
                .filter(HttpBasicAuth.class::isInstance)
                .findFirst()
                .map(HttpBasicAuth.class::cast)
                .orElse(null);
    }

    static RestClient createRestClient() {
        // Create ObjectMapper with JavaTimeModule for proper date/time serialization
        ObjectMapper objectMapper = new ObjectMapper();

        // Build RestClient with custom ObjectMapper and HTTPS support
        HttpComponentsClientHttpRequestFactory requestFactory = getClientHttpRequestFactoryForHttps();

        return ApiClient.buildRestClientBuilder(objectMapper)
                .requestFactory(requestFactory)
                .build();
    }

    static HttpComponentsClientHttpRequestFactory getClientHttpRequestFactoryForHttps() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }

        DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .setDefaultSocketConfig(org.apache.hc.core5.http.io.SocketConfig.custom()
                        .setSoTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(30))
                        .build())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}

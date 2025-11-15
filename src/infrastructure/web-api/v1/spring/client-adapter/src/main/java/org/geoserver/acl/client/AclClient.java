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
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.geoserver.acl.webapi.v1.client.ApiClient;
import org.geoserver.acl.webapi.v1.client.AuthorizationApi;
import org.geoserver.acl.webapi.v1.client.DataRulesApi;
import org.geoserver.acl.webapi.v1.client.WorkspaceAdminRulesApi;
import org.geoserver.acl.webapi.v1.client.auth.HttpBasicAuth;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Client for the GeoServer ACL (Access Control List) REST API.
 * <p>
 * This client provides access to the ACL API endpoints for managing data rules,
 * workspace admin rules, and authorization queries.
 *
 * <h2>Request Logging</h2>
 * <p>
 * To enable debug logging of HTTP requests and responses, configure the Spring
 * RestClient logger at DEBUG level:
 * </p>
 * <pre>
 * # In application.properties:
 * {@literal logging.level.org.springframework.web.client.RestClient=DEBUG}
 *
 * # Or in logback.xml:
 * {@literal <logger name="org.springframework.web.client.RestClient" level="DEBUG">}
 * </pre>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * AclClient client = new AclClient()
 *     .setBasePath("http://localhost:8080/api")
 *     .setUsername("admin")
 *     .setPassword("password");
 *
 * DataRulesApi rulesApi = client.getRulesApi();
 * List<Rule> rules = rulesApi.getRules(null, null).getBody();
 * }
 * </pre>
 */
public class AclClient {

    private ApiClient apiClient;

    /**
     * Creates a new ACL client with default configuration.
     * <p>
     * The client is configured with HTTPS support and accepts all SSL certificates.
     * Use {@link #setBasePath(String)} to configure the API endpoint URL.
     * </p>
     */
    public AclClient() {
        RestClient restClient = createRestClient();
        apiClient = new ApiClient(restClient);
    }

    /**
     * Sets the base path for the ACL API.
     *
     * @param basePath the base URL of the ACL service (e.g., "http://localhost:8080/api")
     * @return this client instance for method chaining
     */
    public AclClient setBasePath(String basePath) {
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
    public AclClient setUsername(String username) {
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
    public AclClient setPassword(String pwd) {
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

    /**
     * Gets the Data Rules API for managing layer access rules.
     *
     * @return the Data Rules API instance
     */
    public DataRulesApi getRulesApi() {
        return new DataRulesApi(apiClient);
    }

    /**
     * Gets the Workspace Admin Rules API for managing workspace administrative access.
     *
     * @return the Workspace Admin Rules API instance
     */
    public WorkspaceAdminRulesApi getAdminRulesApi() {
        return new WorkspaceAdminRulesApi(apiClient);
    }

    /**
     * Gets the Authorization API for querying access permissions.
     *
     * @return the Authorization API instance
     */
    public AuthorizationApi getAuthorizationApi() {
        return new AuthorizationApi(apiClient);
    }

    static RestClient createRestClient() {
        // Create ObjectMapper with JavaTimeModule for proper date/time serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

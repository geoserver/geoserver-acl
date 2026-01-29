/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.geoserver.acl.webapi.v1.model.GrantType;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class AbstractAccesControlListApplicationTest {

    protected final String adminUsername = "admin";
    protected final String adminUserPassword = "s3cr3t";

    protected final String testUsername = "testuser";
    protected final String testUserPassword = "changeme";

    @DynamicPropertySource
    static void registerTestUser(DynamicPropertyRegistry registry) {
        // define a non-admin test user
        registry.add("geoserver.acl.security.internal.users.testuser.enabled", () -> "true");
        registry.add("geoserver.acl.security.internal.users.testuser.admin", () -> "false");
        registry.add("geoserver.acl.security.internal.users.testuser.password", () -> "changeme");
    }

    @Autowired
    TestRestTemplate baseClient;

    // client to be used, may have login credentials
    TestRestTemplate client;

    @BeforeEach
    void setup() {
        // Configure client to not follow redirects so we can test redirect responses
        client = baseClient.withRedirects(Redirects.DONT_FOLLOW);
    }

    @Test
    void getRulesUnauthorizedIfNotLoggedIn() {
        var response = get("/api/rules", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getRulesLoggedInAsNonAdminUser() {
        loginAsUser();
        var response = get("/api/rules", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createAllowRule() {
        loginAsAdmin();
        var response = createRule(
                """
                        {
                          "priority": 0,
                          "access": "ALLOW"
                        }
                        """);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getPriority()).isPositive();
        assertThat(response.getBody().getAccess()).isEqualTo(GrantType.ALLOW);
    }

    @Test
    void createDenyRule() {
        loginAsAdmin();
        var response = createRule(
                """
                        {
                          "priority": 0,
                          "access": "DENY"
                        }
                        """);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getPriority()).isPositive();
        assertThat(response.getBody().getAccess()).isEqualTo(GrantType.DENY);
    }

    protected void loginAsAdmin() {
        login(adminUsername, adminUserPassword);
    }

    protected void loginAsUser() {
        login(testUsername, testUserPassword);
    }

    protected void login(String user, String pwd) {
        client = client.withBasicAuth(user, pwd);
    }

    protected <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        return get(path, responseType, new HttpHeaders());
    }

    protected <T> ResponseEntity<T> get(String path, Class<T> responseType, HttpHeaders headers) {
        if (headers.getAccept().isEmpty()) {
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);

        var url = fullUrl(path);
        return client.exchange(url, HttpMethod.GET, entity, responseType);
    }

    private ResponseEntity<Rule> createRule(String json) {
        return post("/api/rules", json, Rule.class);
    }

    protected <T> ResponseEntity<T> post(
            String path, String requestBodyJson, Class<T> responseType, Object... urlVariables) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

        var url = fullUrl(path);
        return client.postForEntity(url, entity, responseType, urlVariables);
    }

    private String fullUrl(String path) {
        String rootUri = client.getRootUri();
        assertThat(rootUri).endsWith("/acl");
        return rootUri + path;
    }
}

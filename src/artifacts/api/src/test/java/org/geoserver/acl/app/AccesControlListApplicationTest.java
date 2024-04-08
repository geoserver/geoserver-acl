/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.autoconfigure.security.SecurityConfigProperties;
import org.geoserver.acl.autoconfigure.security.SecurityConfigProperties.PreauthHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class AccesControlListApplicationTest extends AbstractAccesControlListApplicationTest {

    @Autowired private SecurityConfigProperties securityConfig;

    @Test
    void rootRedirectsToSwaggerUI() {
        String expected = "/acl/openapi/swagger-ui/index.html";

        ResponseEntity<String> response = get("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().get("Location")).containsExactly(expected);
    }

    @Test
    void rootRedirectsToSwaggerUIWithXForwardedHeaders() {
        var headers = new HttpHeaders();
        headers.add("X-Forwarded-Prefix", "/geoserver/cloud");

        String expected = "/geoserver/cloud/acl/openapi/swagger-ui/index.html";
        ResponseEntity<String> response = get("/", String.class, headers);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);
        assertThat(response.getHeaders().get("Location")).containsExactly(expected);
    }

    @Test
    void preAuthDefaultconfig() {
        PreauthHeaders config = securityConfig.getHeaders();
        assertThat(config.getUserHeader()).isEqualTo("sec-username");
        assertThat(config.getRolesHeader()).isEqualTo("sec-roles");
        assertThat(config.getAdminRoles()).containsExactly("ROLE_ADMINISTRATOR");
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class AccesControlListApplicationIT extends AbstractAccesControlListApplicationTest {

    private static final DockerImageName POSTGIS_IMAGE_NAME =
            DockerImageName.parse("postgis/postgis:14-3.4").asCompatibleSubstituteFor("postgres");

    @Container
    static PostgreSQLContainer<?> postgis = new PostgreSQLContainer<>(POSTGIS_IMAGE_NAME);

    /** Set up the properties defined in values.yml and used as place-holders in application.yml */
    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("pg.host", () -> postgis.getHost());
        registry.add("pg.port", () -> postgis.getFirstMappedPort());
        registry.add("pg.db", () -> postgis.getDatabaseName());
        registry.add("pg.schema", () -> "acltest");
        registry.add("pg.username", postgis::getUsername);
        registry.add("pg.password", postgis::getPassword);
    }
}

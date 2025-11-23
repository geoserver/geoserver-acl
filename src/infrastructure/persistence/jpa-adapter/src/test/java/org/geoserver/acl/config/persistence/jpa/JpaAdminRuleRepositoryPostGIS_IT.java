/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.persistence.jpa;

import jakarta.transaction.Transactional;
import org.geoserver.acl.persistence.jpa.domain.JpaAdminRuleRepositoryTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@Transactional
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AclDataSourceConfiguration.class,
            AuthorizationJPAConfiguration.class
        })
// see config props in src/test/resource/application-test.yaml
@ActiveProfiles("test")
class JpaAdminRuleRepositoryPostGIS_IT extends JpaAdminRuleRepositoryTest {

    private static final DockerImageName POSTGIS_IMAGE_NAME =
            DockerImageName.parse("imresamu/postgis:15-3.4").asCompatibleSubstituteFor("postgres");

    @Container
    static PostgreSQLContainer postgis = new PostgreSQLContainer(POSTGIS_IMAGE_NAME);

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("geoserver.acl.datasource.url", () -> postgis.getJdbcUrl());
        registry.add("geoserver.acl.datasource.username", postgis::getUsername);
        registry.add("geoserver.acl.datasource.password", postgis::getPassword);
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AclDataSourceConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AuthorizationJPAPropertiesTestConfiguration.class, AclDataSourceConfiguration.class);

    @Test
    void testConfigured() {

        runner.withPropertyValues( //
                        "geoserver.acl.datasource.url=jdbc:h2:mem:authorization-test")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasBean("authorizationDataSource");
                    assertThat(context.getBean("authorizationDataSource", DataSource.class)
                                    .getConnection())
                            .isNotNull();
                    assertThat(context.getBean("authorizationDataSource", DataSource.class))
                            .isInstanceOf(HikariDataSource.class);
                    HikariDataSource ds =
                            (HikariDataSource) context.getBean("authorizationDataSource", DataSource.class);
                    assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:h2:mem:authorization-test");
                });
    }

    @Test
    void testUnonfigured() {

        runner.withPropertyValues( //
                        "geoserver.acl.datasource.url=")
                .run(context -> {
                    assertThat(context)
                            .hasFailed()
                            .getFailure()
                            .hasMessageContaining(
                                    "geoserver.acl.datasource.url or geoserver.acl.datasource.jndiName is requried");
                });
    }
}

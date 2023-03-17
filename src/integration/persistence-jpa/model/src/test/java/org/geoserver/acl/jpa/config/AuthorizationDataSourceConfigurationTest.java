package org.geoserver.acl.jpa.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;

class AuthorizationDataSourceConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            AuthorizationJPAPropertiesTestConfiguration.class,
                            AuthorizationDataSourceConfiguration.class);

    @Test
    void testConfigured() {

        runner.withPropertyValues( //
                        "geoserver.acl.datasource.url=jdbc:h2:mem:authorization-test")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed().hasBean("authorizationDataSource");
                            assertThat(
                                            context.getBean(
                                                            "authorizationDataSource",
                                                            DataSource.class)
                                                    .getConnection())
                                    .isNotNull();
                            assertThat(context.getBean("authorizationDataSource", DataSource.class))
                                    .isInstanceOf(HikariDataSource.class);
                            HikariDataSource ds =
                                    (HikariDataSource)
                                            context.getBean(
                                                    "authorizationDataSource", DataSource.class);
                            assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:h2:mem:authorization-test");
                        });
    }

    @Test
    void testUnonfigured() {

        runner.withPropertyValues( //
                        "geoserver.acl.datasource.url=")
                .run(
                        context -> {
                            assertThat(context)
                                    .hasFailed()
                                    .getFailure()
                                    .hasMessageContaining(
                                            "geoserver.acl.datasource.url or geoserver.acl.datasource.jndiName is requried");
                        });
    }
}

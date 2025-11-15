/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.orm.jpa.JpaTransactionManager;

class AuthorizationJPAConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of( //
                    DataSourceAutoConfiguration.class, TestDatabaseAutoConfiguration.class))
            .withUserConfiguration(
                    AuthorizationJPAPropertiesTestConfiguration.class, AuthorizationJPAConfiguration.class);

    @Test
    void testAuthorizationEntityManagerFailsWithMissingDataSource() {
        runner.run(context -> {
            assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .hasMessageContaining("Error creating bean with name 'authorizationEntityManagerFactory'")
                    .cause()
                    .isInstanceOf(NoSuchBeanDefinitionException.class)
                    .hasMessageContaining("authorizationDataSource");
        });
    }

    @Test
    void testAuthorizationEntityManager() {
        runner
                // authorizationDataSource will be replaced by TestDatabaseAutoConfiguration
                .withBean("authorizationDataSource", DataSource.class, () -> mock(DataSource.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("authorizationEntityManagerFactory");
                    assertThat(context.getBean("authorizationEntityManagerFactory"))
                            .isInstanceOf(EntityManagerFactory.class);
                });
    }

    @Test
    void testAuthorizationTransactionManager() {
        runner
                // authorizationDataSource will be replaced by TestDatabaseAutoConfiguration
                .withBean("authorizationDataSource", DataSource.class, () -> mock(DataSource.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("authorizationTransactionManager");
                    assertThat(context.getBean("authorizationTransactionManager"))
                            .isInstanceOf(JpaTransactionManager.class);
                });
    }

    @Test
    void testJpaRepositories() {
        runner
                // authorizationDataSource will be replaced by TestDatabaseAutoConfiguration
                .withBean("authorizationDataSource", DataSource.class, () -> mock(DataSource.class))
                .run(context -> {
                    assertThat(context)
                            .hasNotFailed()
                            .hasSingleBean(JpaRuleRepository.class)
                            .hasSingleBean(JpaAdminRuleRepository.class);
                });
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.config;

import org.geoserver.acl.jpa.config.AuthorizationJPAProperties.JpaProperties;
import org.geoserver.acl.jpa.model.Rule;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories( //
        basePackageClasses = {JpaRuleRepository.class, JpaAdminRuleRepository.class},
        entityManagerFactoryRef = "authorizationEntityManagerFactory",
        transactionManagerRef = "authorizationTransactionManager")
public class AuthorizationJPAConfiguration {

    @Bean("authorizationVendorAdapter")
    HibernateJpaVendorAdapter authorizationVendorAdapter(AuthorizationJPAProperties configProps) {
        JpaProperties jpaConfig = configProps.getJpa();
        HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();

        boolean showSql = jpaConfig.isShowSql();
        boolean generateDdl = jpaConfig.isGenerateDdl();
        String databasePlatform = jpaConfig.getDatabasePlatform();

        va.setGenerateDdl(generateDdl);
        va.setShowSql(showSql);
        va.setDatabasePlatform(databasePlatform);
        return va;
    }

    @Bean("authorizationEntityManagerFactory")
    @DependsOn({"authorizationDataSource", "authorizationVendorAdapter"})
    public LocalContainerEntityManagerFactoryBean authorizationEntityManagerFactory( //
            @Qualifier("authorizationVendorAdapter")
                    HibernateJpaVendorAdapter authorizationVendorAdapter,
            @Qualifier("authorizationDataSource") DataSource dataSource,
            AuthorizationJPAProperties configProps) {

        Map<String, String> jpaProperties = configProps.getJpa().getProperties();

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceUnitName("authorizationPersistentUnit");
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(authorizationVendorAdapter);
        emf.setJpaPropertyMap(jpaProperties);
        emf.setPackagesToScan(Rule.class.getPackage().getName());
        return emf;
    }

    @Bean("authorizationTransactionManager")
    public JpaTransactionManager authorizationTransactionManager(
            @Qualifier("authorizationEntityManagerFactory") final EntityManagerFactory emf) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}

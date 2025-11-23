/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.persistence.jpa;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.geoserver.acl.config.persistence.jpa.AclJpaProperties.JpaProperties;
import org.geoserver.acl.persistence.jpa.domain.JpaAdminRuleRepository;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleRepository;
import org.geoserver.acl.persistence.jpa.model.Rule;
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

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaAuditing
@EnableJpaRepositories( //
        basePackageClasses = {JpaRuleRepository.class, JpaAdminRuleRepository.class},
        entityManagerFactoryRef = "authorizationEntityManagerFactory",
        transactionManagerRef = "authorizationTransactionManager")
public class AuthorizationJPAConfiguration {

    @Bean("authorizationVendorAdapter")
    HibernateJpaVendorAdapter authorizationVendorAdapter(AclJpaProperties configProps) {
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
    LocalContainerEntityManagerFactoryBean authorizationEntityManagerFactory( //
            @Qualifier("authorizationVendorAdapter") HibernateJpaVendorAdapter authorizationVendorAdapter,
            @Qualifier("authorizationDataSource") DataSource dataSource,
            AclJpaProperties configProps) {

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
    JpaTransactionManager authorizationTransactionManager(
            @Qualifier("authorizationEntityManagerFactory") final EntityManagerFactory emf) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}

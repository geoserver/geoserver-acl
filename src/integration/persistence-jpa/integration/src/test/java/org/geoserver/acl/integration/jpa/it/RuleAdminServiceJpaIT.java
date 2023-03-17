package org.geoserver.acl.integration.jpa.it;

import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.rules.AbstractRuleAdminServiceIT;
import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AuthorizationJPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
public class RuleAdminServiceJpaIT extends AbstractRuleAdminServiceIT {

    private @Autowired RuleRepository repo;
    private @Autowired JpaRuleRepository jpaRepo;

    @BeforeEach
    void setUp() {
        jpaRepo.deleteAll();
        super.ruleAdminService = new RuleAdminService(repo);
    }
}

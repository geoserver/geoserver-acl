package org.geoserver.acl.integration.jpa.it;

import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleAdminServiceIT;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            JPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
public class RuleAdminServiceJpaIT extends RuleAdminServiceIT {

    private @Autowired RuleRepository repo;
    private @Autowired JpaRuleRepository jpaRepo;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        jpaRepo.deleteAll();
        super.setUp();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminService(repo);
    }
}

package org.geoserver.acl.integration.jpa.it;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImpl_GeomTest;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration
                    .class,
            JPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
public class AuthorizationServiceImpl_GeomJpaIT extends AuthorizationServiceImpl_GeomTest {

    private @Autowired JpaIntegrationTestSupport support;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        support.setUp();
        super.setUp();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return support.getRuleAdminService();
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return support.getAdminruleAdminService();
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return support.getAuthorizationService();
    }
}

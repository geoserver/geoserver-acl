package org.geoserver.acl.integration.jpa.it;

import org.geoserver.acl.authorization.AbstractRuleReaderServiceImpl_GeomTest;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration
                    .class,
            AuthorizationJPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
public class RuleReaderServiceImpl_GeomJpaIT extends AbstractRuleReaderServiceImpl_GeomTest {

    private @Autowired JpaIntegrationTestSupport support;

    @BeforeEach
    void setUp() {
        support.setUp();

        super.adminruleAdminService = support.getAdminruleAdminService();
        super.ruleAdminService = support.getRuleAdminService();
        super.ruleReaderService = support.getRuleReaderService();
    }
}

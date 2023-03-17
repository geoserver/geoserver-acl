package org.geoserver.acl.integration.jpa.it;

import org.geoserver.acl.authorization.AbstractRuleReaderServiceImplTest;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * RuleReaderServiceImpl integration test with JPA-backed repositories
 *
 * <pre>{@code
 *                 RuleReaderService
 *                   |          |
 *                   v          v
 *         RuleAdminService  AdminRuleAdminService
 *             |                     |
 *             v                     v
 * RuleRepositoryJpaAdaptor  AdminRuleRepositoryJpaAdaptor
 *              \                   /
 *               \                 /
 *                \               /
 *                 \_____________/
 *                 |             |
 *                 |  Database   |
 *                 |_____________|
 *
 * }</pre>
 *
 * @since 4.0
 */
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AuthorizationJPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
public class RuleReaderServiceImplJpaIT extends AbstractRuleReaderServiceImplTest {

    private @Autowired JpaIntegrationTestSupport support;

    @BeforeEach
    void setUp() {
        support.setUp();

        super.adminruleAdminService = support.getAdminruleAdminService();
        super.ruleAdminService = support.getRuleAdminService();
        super.ruleReaderService = support.getRuleReaderService();
    }
}

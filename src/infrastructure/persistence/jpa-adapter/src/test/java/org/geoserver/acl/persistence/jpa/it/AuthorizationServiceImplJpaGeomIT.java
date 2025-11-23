/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.it;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceGeomTest;
import org.geoserver.acl.config.persistence.jpa.JPAIntegrationConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            org.geoserver.acl.config.persistence.jpa.AuthorizationJPAPropertiesTestConfiguration.class,
            JPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
@DirtiesContext
class AuthorizationServiceImplJpaGeomIT extends AuthorizationServiceGeomTest {

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

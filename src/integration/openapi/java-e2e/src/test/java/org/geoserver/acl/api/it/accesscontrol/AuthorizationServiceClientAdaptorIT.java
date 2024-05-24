/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.accesscontrol;

import org.geoserver.acl.api.client.integration.AuthorizationServiceClientAdaptor;
import org.geoserver.acl.api.it.support.ClientContextSupport;
import org.geoserver.acl.api.it.support.IntegrationTestsApplication;
import org.geoserver.acl.api.it.support.ServerContextSupport;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceTest;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

/**
 * {@link AuthorizationService} end to end integration test with {@link
 * AuthorizationServiceClientAdaptor} hitting the authorization API directly through HTTP.
 *
 * <pre>{@code
 * CLIENT:            AuthorizationServiceClientAdaptor <<AuthorizationService>>
 *                                 |
 *                                 |
 *                                 | <HTTP>
 * SERVER:                         |
 *                                 v
 *                      AuthorizationApiController <codegen>
 *                                 |
 *                                 v
 *                        AuthorizationApiImpl
 *                                 |
 *                                 v
 *                       AuthorizationServiceImpl
 *                        |                  |
 *                        |                  |
 *                        v                  v
 *               RuleAdminService       AdminRuleAdminService
 *                    |                         |
 *                    v                         v
 *        RuleRepositoryJpaAdaptor      AdminRuleRepositoryJpaAdaptor
 *                       \                   /
 *                        \                 /
 *                         \               /
 *                          \_____________/
 *                          |             |
 *                          |  Database   |
 *                          |_____________|
 *
 * }</pre>
 *
 * @since 1.0
 * @see AuthorizationServiceImplTest
 */
@DirtiesContext
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
            "geoserver.acl.jpa.show-sql=false",
            "geoserver.acl.jpa.properties.hibernate.hbm2ddl.auto=create",
            "geoserver.acl.datasource.url=jdbc:h2:mem:geoserver-acl"
        },
        classes = {IntegrationTestsApplication.class})
class AuthorizationServiceClientAdaptorIT extends AuthorizationServiceTest {

    private @Autowired ServerContextSupport serverContext;
    private @LocalServerPort int serverPort;

    private ClientContextSupport clientContext;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        clientContext = new ClientContextSupport().log(false).serverPort(serverPort).setUp();
        serverContext.setUp();
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        clientContext.close();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return clientContext.getRuleAdminServiceClient();
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return clientContext.getAdminRuleAdminServiceClient();
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return clientContext.getAuthorizationServiceClientAdaptor();
    }
}

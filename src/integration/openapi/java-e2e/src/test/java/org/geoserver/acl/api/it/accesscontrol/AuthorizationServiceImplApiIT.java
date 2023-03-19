/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.accesscontrol;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.api.it.support.ClientContextSupport;
import org.geoserver.acl.api.it.support.IntegrationTestsApplication;
import org.geoserver.acl.api.it.support.ServerContextSupport;
import org.geoserver.acl.authorization.AuthorizationServiceImplTest;
import org.geoserver.acl.model.authorization.AuthorizationService;
import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

/**
 * {@link AuthorizationService} end to end integration test with {@link AuthorizationService}
 * hitting {@link RuleAdminService} and {@link AdminRuleAdminService} backed by OpenAPI Java Client
 * adaptors for {@link RuleRepository} and {@link AdminRuleRepository}, which in turn make real HTTP
 * calls to a running server API.
 *
 * <pre>{@code
 * CLIENT:                     AuthorizationService
 *                                |          |
 *                                v          v
 *                     RuleAdminService  AdminRuleAdminService
 *                          |                    |
 *                          v                    v
 *         RuleRepositoryClientAdaptor    AdminRuleRepositoryClientAdaptor
 *                    |                             |
 *                    v                             v
 *                RulesApi <codegen>           AdminRulesApi <codegen>
 *                    |                             |
 *                    |                             |
 *                    | <HTTP>                      | <HTTP>
 * SERVER:            |                             |
 *                    v                             v
 *      RulesApiImplController <codegen>   AdminRulesApiController <codegen>
 *                    |                             |
 *                    v                             v
 *              RulesApiImpl                 AdminRulesApiImpl
 *                    |                             |
 *                    v                             v
 *            RuleAdminService            AdminRuleAdminService
 *                    |                             |
 *                    v                             v
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
public class AuthorizationServiceImplApiIT extends AuthorizationServiceImplTest {

    private @Autowired ServerContextSupport serverContext;
    private @LocalServerPort int serverPort;

    private ClientContextSupport clientContext;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        clientContext =
                new ClientContextSupport()
                        // logging breaks client exception handling, only enable if need to see the
                        // request/response bodies
                        .log(false)
                        .serverPort(serverPort)
                        .setUp();
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
        return clientContext.getAuthorizationService();
    }
}

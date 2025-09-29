/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.rules;

import org.geoserver.acl.api.it.support.ClientContextSupport;
import org.geoserver.acl.api.it.support.IntegrationTestsApplication;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceIT;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
            "geoserver.acl.jpa.show-sql=false",
            "geoserver.acl.jpa.properties.hibernate.hbm2ddl.auto=create",
            "geoserver.acl.datasource.url=jdbc:h2:mem:geoserver-acl"
        },
        classes = {IntegrationTestsApplication.class})
class RuleAdminServiceApiIT extends RuleAdminServiceIT {

    private @Autowired ApplicationContext serverContext;
    private @LocalServerPort int serverPort;

    private ClientContextSupport clientContext;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        clientContext = new ClientContextSupport()
                // logging breaks client exception handling, only enable if need to see the
                // request/response bodies
                .log(false)
                .serverPort(serverPort)
                .setUp();
        JpaRuleRepository jparepo = serverContext.getBean(JpaRuleRepository.class);
        jparepo.deleteAll();
        super.setUp();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return clientContext.getRuleAdminServiceClient();
    }

    @AfterEach
    void tearDown() {
        clientContext.close();
    }
}

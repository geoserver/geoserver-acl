package org.geoserver.acl.integration.jpa.it;

import lombok.Getter;

import org.geoserver.acl.authorization.AuthorizationServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaIntegrationTestSupport {

    private @Autowired @Getter AdminRuleRepository adminRuleRepository;
    private @Autowired @Getter JpaAdminRuleRepository jpaAdminRules;

    private @Autowired @Getter RuleRepository ruleRepository;
    private @Autowired @Getter JpaRuleRepository jpaRules;

    private @Getter AdminRuleAdminService adminruleAdminService;
    private @Getter RuleAdminService ruleAdminService;
    private @Getter AuthorizationServiceImpl authorizationService;

    public void setUp() {
        jpaAdminRules.deleteAll();
        jpaRules.deleteAll();

        adminruleAdminService = new AdminRuleAdminServiceImpl(adminRuleRepository);
        ruleAdminService = new RuleAdminServiceImpl(ruleRepository);

        authorizationService =
                new AuthorizationServiceImpl(adminruleAdminService, ruleAdminService);
    }

    public void tearDown() {}
}

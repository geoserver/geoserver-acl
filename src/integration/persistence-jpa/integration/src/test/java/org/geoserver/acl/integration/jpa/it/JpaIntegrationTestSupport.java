package org.geoserver.acl.integration.jpa.it;

import lombok.Getter;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.authorization.RuleReaderServiceImpl;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleRepository;
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
    private @Getter RuleReaderServiceImpl ruleReaderService;

    public void setUp() {
        jpaAdminRules.deleteAll();
        jpaRules.deleteAll();

        adminruleAdminService = new AdminRuleAdminService(adminRuleRepository);
        ruleAdminService = new RuleAdminService(ruleRepository);

        ruleReaderService = new RuleReaderServiceImpl(adminruleAdminService, ruleAdminService);
    }

    public void tearDown() {}
}

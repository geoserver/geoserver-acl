/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */
package org.geoserver.acl.authorization;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.rules.MemoryRuleRepository;
import org.geoserver.acl.rules.RuleAdminService;
import org.junit.jupiter.api.BeforeEach;

public class RuleReaderServiceImpl_GeomTest extends AbstractRuleReaderServiceImpl_GeomTest {

    @BeforeEach
    void setUp() {
        super.ruleAdminService = new RuleAdminService(new MemoryRuleRepository());
        super.adminruleAdminService = new AdminRuleAdminService(new MemoryAdminRuleRepository());

        super.ruleReaderService =
                new RuleReaderServiceImpl(adminruleAdminService, ruleAdminService);
    }
}

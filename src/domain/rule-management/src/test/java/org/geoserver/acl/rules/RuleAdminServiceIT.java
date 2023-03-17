/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.rules;

import org.junit.jupiter.api.BeforeEach;

public class RuleAdminServiceIT extends AbstractRuleAdminServiceIT {

    @BeforeEach
    void setUp() {
        super.ruleAdminService = new RuleAdminService(new MemoryRuleRepository());
    }
}

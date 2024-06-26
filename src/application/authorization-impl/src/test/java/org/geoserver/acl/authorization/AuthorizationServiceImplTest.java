/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.domain.rules.MemoryRuleRepository;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;

/**
 * {@link AuthorizationService} integration/conformance test
 *
 * <p>Concrete implementations must supply the required services in {@link ServiceTestBase}
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
class AuthorizationServiceImplTest extends AuthorizationServiceTest {

    @Override
    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminServiceImpl(new MemoryRuleRepository());
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return new AdminRuleAdminServiceImpl(new MemoryAdminRuleRepository());
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return new AuthorizationServiceImpl(super.adminruleAdminService, super.ruleAdminService);
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.domain;

import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RuleAdminServiceConfiguration {

    @Bean
    public RuleAdminService ruleAdminService(RuleRepository ruleRepository) {
        return new RuleAdminService(ruleRepository);
    }
}

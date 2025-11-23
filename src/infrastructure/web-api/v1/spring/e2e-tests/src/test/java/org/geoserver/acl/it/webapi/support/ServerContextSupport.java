/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.it.webapi.support;

import org.geoserver.acl.persistence.jpa.domain.JpaAdminRuleRepository;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerContextSupport {

    private @Autowired JpaRuleRepository jpaRuleRepository;
    private @Autowired JpaAdminRuleRepository jpaAdminRuleRepository;

    public void setUp() {
        jpaRuleRepository.deleteAll();
        jpaAdminRuleRepository.deleteAll();
    }
}

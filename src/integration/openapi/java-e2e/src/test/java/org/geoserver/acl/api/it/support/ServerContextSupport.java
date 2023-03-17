/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.support;

import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaGeoServerInstanceRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerContextSupport {

    private @Autowired JpaRuleRepository jpaRuleRepository;
    private @Autowired JpaAdminRuleRepository jpaAdminRuleRepository;
    private @Autowired JpaGeoServerInstanceRepository jpaGeoServerInstanceRepository;

    public void setUp() {
        jpaRuleRepository.deleteAll();
        jpaAdminRuleRepository.deleteAll();
        jpaGeoServerInstanceRepository.deleteAll();
    }
}

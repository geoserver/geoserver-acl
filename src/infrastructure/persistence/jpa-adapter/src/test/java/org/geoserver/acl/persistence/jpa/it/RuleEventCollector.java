/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.it;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.springframework.context.event.EventListener;

public class RuleEventCollector {

    private @Getter List<RuleEvent> ruleEvents = new ArrayList<>();
    private @Getter List<AdminRuleEvent> adminRuleEvents = new ArrayList<>();

    @EventListener(RuleEvent.class)
    public synchronized void collectRuleEvent(RuleEvent event) {
        ruleEvents.add(event);
    }

    @EventListener(AdminRuleEvent.class)
    public synchronized void collectAdminRuleEvent(AdminRuleEvent event) {
        adminRuleEvents.add(event);
    }
}

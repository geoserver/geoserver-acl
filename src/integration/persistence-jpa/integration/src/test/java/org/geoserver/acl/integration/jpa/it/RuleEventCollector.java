package org.geoserver.acl.integration.jpa.it;

import lombok.Getter;

import org.geoserver.acl.domain.event.AdminRuleEvent;
import org.geoserver.acl.domain.event.RuleEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

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

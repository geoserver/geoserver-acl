/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.css;

import org.apache.wicket.markup.html.WebPage;
import org.geoserver.acl.plugin.web.accessrules.AccessRulesACLPage;
import org.geoserver.acl.plugin.web.accessrules.DataAccessRuleEditPage;
import org.geoserver.acl.plugin.web.adminrules.AdminRuleEditPage;
import org.geoserver.acl.plugin.web.adminrules.AdminRulesACLPage;
import org.geoserver.web.HeaderContribution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SuppressWarnings("unchecked")
public class CSSConfiguration {

    /**
     * Contributes {@code RulesTablePanel.css} to {@link AccessRulesACLPage} and {@link
     * AdminRulesACLPage}
     */
    @Bean
    HeaderContribution aclRulesPageCssContribution() {
        return new CssContribution(
                "RulesTablePanel.css", AccessRulesACLPage.class, AdminRulesACLPage.class);
    }

    /** Contributes {@code DataAccessRuleEditPage.css} to {@link DataAccessRuleEditPage} */
    @Bean
    HeaderContribution aclDataAccessRuleEditPageCss() {
        return new CssContribution("DataAccessRuleEditPage.css", DataAccessRuleEditPage.class);
    }

    /**
     * Contributes {@code switch-field.css} to {@link DataAccessRuleEditPage} and {@link
     * AdminRuleEditPage}
     */
    @Bean
    HeaderContribution aclSwitchFieldCss() {
        return new CssContribution(
                "switch-field.css", DataAccessRuleEditPage.class, AdminRuleEditPage.class);
    }

    static class CssContribution extends HeaderContribution {

        private List<Class<? extends WebPage>> appliesTo;

        CssContribution(String cssFile, Class<? extends WebPage>... pages) {
            this.appliesTo = List.of(pages);
            setCSSFilename(cssFile);
            setScope(CSSConfiguration.class);
        }

        public @Override boolean appliesTo(WebPage page) {
            return appliesTo.stream().anyMatch(c -> c.isInstance(page));
        }
    }
}

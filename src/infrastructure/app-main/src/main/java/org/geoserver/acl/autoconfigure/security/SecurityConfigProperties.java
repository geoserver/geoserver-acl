/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@ConfigurationProperties(value = SecurityConfigProperties.PREFIX)
public class SecurityConfigProperties {

    public static final String PREFIX = "geoserver.acl.security";

    private Internal internal = new Internal();
    private PreauthHeaders headers = new PreauthHeaders();

    public boolean enabled() {
        return internal.isEnabled() || headers.isEnabled();
    }

    public static @Data class Internal {
        private boolean enabled;
        private Map<String, UserInfo> users = Map.of();

        @ToString(exclude = "password")
        public static @Data class UserInfo {
            private String password;
            private boolean admin;
            private boolean enabled = true;

            public List<SimpleGrantedAuthority> authorities() {
                return Stream.of(admin ? "ROLE_ADMIN" : "ROLE_USER")
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }
        }
    }

    public static @Data class PreauthHeaders {
        private boolean enabled;
        private String userHeader = "sec-username";
        private String rolesHeader = "sec-roles";
        private List<String> adminRoles = List.of("ADMIN");
    }
}

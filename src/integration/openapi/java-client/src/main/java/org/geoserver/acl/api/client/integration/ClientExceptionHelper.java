/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.web.client.HttpClientErrorException;

@UtilityClass
class ClientExceptionHelper {

    static String reason(HttpClientErrorException e) {
        return reason(e, e.getMessage());
    }

    static String reason(HttpClientErrorException e, String defaultValue) {
        return Optional.ofNullable(e.getResponseHeaders())
                .map(h -> h.getFirst("X-Reason"))
                .orElse(defaultValue);
    }
}

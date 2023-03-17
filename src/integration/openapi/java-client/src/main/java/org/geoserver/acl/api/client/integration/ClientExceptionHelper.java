/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import org.springframework.web.client.HttpClientErrorException;

class ClientExceptionHelper {

    static String reason(HttpClientErrorException e) {
        return reason(e, e.getMessage());
    }

    static String reason(HttpClientErrorException e, String defaultValue) {
        String reason = e.getResponseHeaders().getFirst("X-Reason");
        return reason == null ? defaultValue : reason;
    }
}

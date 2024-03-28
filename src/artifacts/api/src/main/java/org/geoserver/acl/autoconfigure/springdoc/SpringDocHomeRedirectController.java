/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
class SpringDocHomeRedirectController {

    private final @NonNull NativeWebRequest req;

    @GetMapping(value = "/")
    public String redirectToSwaggerUI() {
        String url = ((HttpServletRequest) req.getNativeRequest()).getRequestURL().toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.path("openapi/swagger-ui/index.html");
        String fullUrl = builder.build().toString();
        String xForwardedPrefixUrl = SpringDocAutoConfiguration.customizeUrl(fullUrl, req);
        return "redirect:" + xForwardedPrefixUrl;
    }
}

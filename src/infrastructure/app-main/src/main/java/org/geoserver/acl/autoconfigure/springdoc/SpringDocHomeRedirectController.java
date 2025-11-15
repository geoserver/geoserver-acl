/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.NativeWebRequest;

@Controller
@RequiredArgsConstructor
class SpringDocHomeRedirectController {

    private final @NonNull NativeWebRequest req;
    private final @NonNull String servletContextPath;

    @GetMapping(value = {"", "/"})
    public String redirectToSwaggerUI() {
        var target = "/openapi/swagger-ui/index.html";
        URI url = URI.create(
                ((HttpServletRequest) req.getNativeRequest()).getRequestURL().toString());
        var path = url.getPath();
        if (path != null) {
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            if (!path.endsWith(servletContextPath)) target = servletContextPath + target;
        }
        return "redirect:%s".formatted(target);
    }
}

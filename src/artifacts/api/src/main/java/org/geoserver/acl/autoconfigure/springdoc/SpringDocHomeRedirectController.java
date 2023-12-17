/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringDocHomeRedirectController {

    private String basePath;

    /**
     * @param basePath e.g. {@literal /swagger-ui/index.html"}
     */
    public SpringDocHomeRedirectController(String basePath) {
        this.basePath = basePath;
    }

    @GetMapping(value = "/")
    public String redirectToSwaggerUI() {
        return "redirect:" + basePath;
    }
}

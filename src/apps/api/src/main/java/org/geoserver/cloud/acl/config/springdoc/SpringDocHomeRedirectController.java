/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.acl.config.springdoc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class SpringDocHomeRedirectController {

    private String basePath;

    /**
     * @param basePath e.g. {@literal /api/v2/swagger-ui/index.html"}
     */
    public SpringDocHomeRedirectController(String basePath) {
        this.basePath = basePath;
    }

    @RequestMapping(value = "/")
    public String redirectToSwaggerUI() {
        return "redirect:" + basePath;
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.providers.SpringWebProvider;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/** {@link AutoConfiguration} redirect the home page to the swagger-ui */
@AutoConfiguration
@Slf4j(topic = "org.geoserver.acl.autoconfigure.springdoc")
public class SpringDocAutoConfiguration {

    private @Value("${server.servlet.context-path:/}") String servletContextPath;

    @Bean
    SpringDocHomeRedirectController homeRedirectController(NativeWebRequest req) {
        return new SpringDocHomeRedirectController(req, servletContextPath);
    }

    @Bean
    ServerBaseUrlCustomizer xForwardedPrefixAwareServerBaseUrlCustomizer() {
        return new ServletContextSuffixingBaseUrlCustomizer(servletContextPath);
    }

    /**
     * Override the one defined in {@link SwaggerConfig} to append the servlet-context path suffix
     * to URLs if they don't have it
     */
    @Bean
    SwaggerWelcomeWebMvc xForwardedPrefixAwareSwaggerWelcome(
            SwaggerUiConfigProperties swaggerUiConfig,
            SpringDocConfigProperties springDocConfigProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SpringWebProvider springWebProvider) {
        return new ServletContextSuffixingSwaggerWelcomeWebMvc(
                swaggerUiConfig,
                springDocConfigProperties,
                swaggerUiConfigParameters,
                springWebProvider,
                servletContextPath);
    }

    @RequiredArgsConstructor
    static class ServletContextSuffixingBaseUrlCustomizer implements ServerBaseUrlCustomizer {
        private final @NonNull String servletContextPath;

        @Override
        public String customize(String serverBaseUrl) {
            String url = serverBaseUrl;
            String path = URI.create(serverBaseUrl).getPath();
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            if (!path.endsWith(servletContextPath)) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverBaseUrl);
                builder.path(servletContextPath);
                url = builder.build().toString();
            }

            return url;
        }
    }

    static class ServletContextSuffixingSwaggerWelcomeWebMvc extends SwaggerWelcomeWebMvc {

        private final @NonNull String servletContextPath;

        public ServletContextSuffixingSwaggerWelcomeWebMvc(
                SwaggerUiConfigProperties swaggerUiConfig,
                SpringDocConfigProperties springDocConfigProperties,
                SwaggerUiConfigParameters swaggerUiConfigParameters,
                SpringWebProvider springWebProvider,
                String contextPath) {
            super(
                    swaggerUiConfig,
                    springDocConfigProperties,
                    swaggerUiConfigParameters,
                    springWebProvider);
            this.servletContextPath = contextPath;
        }

        @Override
        protected String buildUrl(String contextPath, final String docsUrl) {
            String realContextPath = contextPath;

            if (!realContextPath.endsWith(this.servletContextPath))
                realContextPath += this.servletContextPath;
            var url = super.buildUrl(realContextPath, docsUrl);
            log.debug("buildUrl({}, {}): {}", contextPath, docsUrl, url);
            return url;
        }
    }
}

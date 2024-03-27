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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/** {@link AutoConfiguration} redirect the home page to the swagger-ui */
@AutoConfiguration
@Slf4j(topic = "org.geoserver.acl.autoconfigure.springdoc")
public class SpringDocAutoConfiguration {

    @Bean
    SpringDocHomeRedirectController homeRedirectController(NativeWebRequest req) {
        return new SpringDocHomeRedirectController(req);
    }

    @Bean
    ServerBaseUrlCustomizer xForwardedPrefixAwareServerBaseUrlCustomizer(NativeWebRequest req) {
        return new XForwardedPrefixBaseUrlCustomizer(req);
    }

    /**
     * Override the one defined in {@link SwaggerConfig} to apply the{@literal X-Forwarded-Prefix}
     * request header prefix to the swagger ui config urls
     */
    @Bean
    SwaggerWelcomeWebMvc xForwardedPrefixAwareSwaggerWelcome(
            SwaggerUiConfigProperties swaggerUiConfig,
            SpringDocConfigProperties springDocConfigProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SpringWebProvider springWebProvider,
            NativeWebRequest nativeWebRequest) {
        return new XForwardedPrefixAwareSwaggerWelcomeWebMvc(
                swaggerUiConfig,
                springDocConfigProperties,
                swaggerUiConfigParameters,
                springWebProvider,
                nativeWebRequest);
    }

    /**
     * Springdoc {@link ServerBaseUrlCustomizer} to apply the {@literal X-Forwarded-Prefix} request
     * header prefix to the base server url presented in the swagger-
     */
    @RequiredArgsConstructor
    static class XForwardedPrefixBaseUrlCustomizer implements ServerBaseUrlCustomizer {
        private final @NonNull NativeWebRequest req;

        @Override
        public String customize(String serverBaseUrl) {
            return customizeUrl(serverBaseUrl, req);
        }
    }

    static class XForwardedPrefixAwareSwaggerWelcomeWebMvc extends SwaggerWelcomeWebMvc {

        private final NativeWebRequest nativeWebRequest;

        public XForwardedPrefixAwareSwaggerWelcomeWebMvc(
                SwaggerUiConfigProperties swaggerUiConfig,
                SpringDocConfigProperties springDocConfigProperties,
                SwaggerUiConfigParameters swaggerUiConfigParameters,
                SpringWebProvider springWebProvider,
                NativeWebRequest nativeWebRequest) {
            super(
                    swaggerUiConfig,
                    springDocConfigProperties,
                    swaggerUiConfigParameters,
                    springWebProvider);
            this.nativeWebRequest = nativeWebRequest;
        }

        @Override
        protected String buildApiDocUrl() {
            var url = super.buildApiDocUrl();
            url = applyForwardedPrefix(url, nativeWebRequest);
            log.debug("buildApiDocUrl: {}", url);
            return url;
        }

        @Override
        protected String buildSwaggerConfigUrl() {
            var url = super.buildSwaggerConfigUrl();
            url = applyForwardedPrefix(url, nativeWebRequest);
            log.debug("buildSwaggerConfigUrl: {}", url);
            return url;
        }

        @Override
        protected String buildUrl(String contextPath, final String docsUrl) {
            var url = super.buildUrl(contextPath, docsUrl);
            url = applyForwardedPrefix(url, nativeWebRequest);
            log.debug("buildUrl({}, {}): {}", contextPath, docsUrl, url);
            return url;
        }

        @Override
        protected String buildUrlWithContextPath(String swaggerUiUrl) {
            var url = super.buildUrlWithContextPath(swaggerUiUrl);
            url = applyForwardedPrefix(url, nativeWebRequest);
            log.debug("buildUrlWithContextPath({}): {}", swaggerUiUrl, url);
            return url;
        }
    }

    private static String applyForwardedPrefix(String path, NativeWebRequest req) {
        String prefix = getFirstHeader(req, "X-Forwarded-Prefix");
        if (null != prefix && !path.startsWith(prefix)) {
            return prefix + path;
        }
        return path;
    }

    private static String getFirstHeader(NativeWebRequest req, String headerName) {
        String[] headerValues = req.getHeaderValues(headerName);
        final String value;
        if (null != headerValues && headerValues.length > 0) {
            value = headerValues[0];
        } else {
            value = null;
        }
        return value;
    }

    /**
     * Applies the {@literal X-Forwarded-Prefix} header prefix to a full URL, if provided in the
     * request
     */
    static String customizeUrl(String url, NativeWebRequest req) {
        String path = URI.create(url).getPath();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        String prefixedPath = applyForwardedPrefix(path, req);
        builder.replacePath(prefixedPath);
        return builder.build().toString();
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.geoserver.acl.api.server.AuthorizationApiController;
import org.geoserver.acl.api.server.AuthorizationApiDelegate;
import org.geoserver.acl.authorization.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.context.request.NativeWebRequest;

class AuthorizationApiConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(UserConfigurations.of(AuthorizationApiConfiguration.class));

    private ApplicationContextRunner withMockRepositories() {
        runner = withMock(NativeWebRequest.class);
        runner = withMock(AuthorizationService.class);
        return runner;
    }

    private <T> ApplicationContextRunner withMock(Class<T> beanType) {
        return runner.withBean(beanType, () -> mock(beanType));
    }

    @Test
    void testWithAvailableAuthorizationService() {
        withMockRepositories().run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(AuthorizationApiController.class)
                    .hasSingleBean(AuthorizationApiDelegate.class);
        });
    }

    @Test
    void testMissingAuthorizationService() {
        runner.run(context -> {
            assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .hasMessageContaining("Unsatisfied dependency")
                    .hasMessageContaining("AuthorizationService");
        });
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImplTest;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleIdentifier;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@link AuthorizationService} integration test with JPA-backed repositories
 *
 * <pre>{@code
 *                AuthorizationService
 *                   |          |
 *                   v          v
 *         RuleAdminService  AdminRuleAdminService
 *             |                     |
 *             v                     v
 * RuleRepositoryJpaAdaptor  AdminRuleRepositoryJpaAdaptor
 *              \                   /
 *               \                 /
 *                \               /
 *                 \_____________/
 *                 |             |
 *                 |  Database   |
 *                 |_____________|
 *
 * }</pre>
 *
 * @since 1.0
 */
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            JPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
@DirtiesContext
class AuthorizationServiceImplJpaIT extends AuthorizationServiceImplTest {

    private @Autowired JpaIntegrationTestSupport support;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        support.setUp();
        super.setUp();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return support.getRuleAdminService();
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return support.getAdminruleAdminService();
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return support.getAuthorizationService();
    }

    /**
     * Test for race condition when multiple threads try to create rules with the same priority.
     * See https://github.com/geoserver/geoserver-acl/issues/84
     */
    @Test
    void testConcurrentRuleCreationWithSamePriority() throws Exception {
        final long PRIORITY = 60000000L;
        final int THREAD_COUNT = 4;

        // Create a latch to synchronize thread starts for maximum contention
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Rule>> futures = new ArrayList<>();

        try {
            // Submit all tasks
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int index = i;
                Future<Rule> future = executor.submit(() -> {
                    try {
                        // Wait for all threads to be ready
                        startLatch.await();

                        // Create rule with same priority
                        RuleIdentifier identifier = RuleIdentifier.builder()
                                .username(null)
                                .rolename("role" + index)
                                .workspace("workspace1")
                                .layer("layer" + index)
                                .access(ALLOW)
                                .build();

                        Rule rule = Rule.builder()
                                .priority(PRIORITY)
                                .identifier(identifier)
                                .build();

                        return ruleAdminService.insert(rule);
                    } finally {
                        doneLatch.countDown();
                    }
                });
                futures.add(future);
            }

            // Release all threads at once to maximize contention
            startLatch.countDown();

            // Wait for all to complete (with timeout)
            assertThat(doneLatch.await(10, TimeUnit.SECONDS))
                    .as("All threads should complete within 10 seconds")
                    .isTrue();

            // Collect rule IDs from creation responses
            List<String> createdRuleIds = new ArrayList<>();
            for (Future<Rule> future : futures) {
                Rule rule = future.get(15, TimeUnit.SECONDS);
                assertThat(rule).as("All rules should be created successfully").isNotNull();
                createdRuleIds.add(rule.getId());
            }

            // Verify all rules were created
            assertThat(createdRuleIds).hasSize(THREAD_COUNT);

            // Re-fetch all rules from the database to get their FINAL priorities
            // (after any shifting that occurred)
            List<Rule> allRules = ruleAdminService.getAll().toList();
            List<Rule> createdRules = allRules.stream()
                    .filter(r -> createdRuleIds.contains(r.getId()))
                    .toList();

            // Extract final priorities from database
            List<Long> priorities =
                    createdRules.stream().map(Rule::getPriority).sorted().toList();

            // The key assertion: all priorities should be different
            assertThat(priorities)
                    .as("All rules should have different priorities (no duplicates)")
                    .doesNotHaveDuplicates();

            // They should be consecutive starting from PRIORITY
            List<Long> expected = List.of(PRIORITY, PRIORITY + 1, PRIORITY + 2, PRIORITY + 3);
            assertThat(priorities)
                    .as("Priorities should be consecutive: %s", expected)
                    .isEqualTo(expected);

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                // Wait a bit for tasks to respond to being cancelled
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        }
    }
}

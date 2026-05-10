/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.config.persistence.jpa.JPAIntegrationConfiguration;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests concurrent priority operations against H2 using {@code pg_advisory_xact_lock}.
 *
 * @see <a href="https://github.com/geoserver/geoserver-acl/issues/84">#84</a>
 */
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            JPAIntegrationConfiguration.class,
            JpaIntegrationTestSupport.class
        })
@ActiveProfiles("test")
abstract class ConcurrentPriorityBaseTest {

    private @Autowired JpaIntegrationTestSupport support;

    @BeforeEach
    void setUp() {
        support.setUp();
    }

    @Test
    void testConcurrentRuleCreationWithSamePriority() throws Exception {
        RuleAdminService ruleAdminService = support.getRuleAdminService();
        final long PRIORITY = 60000000L;
        final int THREAD_COUNT = 8;

        List<Rule> created = runConcurrent(THREAD_COUNT, index -> {
            RuleIdentifier identifier = RuleIdentifier.builder()
                    .username(null)
                    .rolename("role" + index)
                    .workspace("workspace1")
                    .layer("layer" + index)
                    .access(ALLOW)
                    .build();

            return ruleAdminService.insert(
                    Rule.builder().priority(PRIORITY).identifier(identifier).build());
        });

        List<Long> priorities = ruleAdminService.getAll().toList().stream()
                .filter(r -> created.stream().anyMatch(c -> c.id().equals(r.id())))
                .map(Rule::priority)
                .sorted()
                .toList();

        assertThat(priorities)
                .doesNotHaveDuplicates()
                .isEqualTo(LongStream.range(PRIORITY, PRIORITY + THREAD_COUNT)
                        .boxed()
                        .toList());
    }

    @Test
    void testConcurrentAdminRuleCreationWithSamePriority() throws Exception {
        AdminRuleAdminService adminRuleAdminService = support.getAdminruleAdminService();
        final long PRIORITY = 70000000L;
        final int THREAD_COUNT = 8;

        List<AdminRule> created = runConcurrent(THREAD_COUNT, index -> {
            AdminRuleIdentifier identifier = AdminRuleIdentifier.builder()
                    .username(null)
                    .rolename("adminrole" + index)
                    .workspace("workspace1")
                    .build();

            return adminRuleAdminService.insert(AdminRule.builder()
                    .priority(PRIORITY)
                    .access(AdminGrantType.ADMIN)
                    .identifier(identifier)
                    .build());
        });

        List<Long> priorities = adminRuleAdminService.getAll().toList().stream()
                .filter(r -> created.stream().anyMatch(c -> c.id().equals(r.id())))
                .map(AdminRule::priority)
                .sorted()
                .toList();

        assertThat(priorities)
                .doesNotHaveDuplicates()
                .isEqualTo(LongStream.range(PRIORITY, PRIORITY + THREAD_COUNT)
                        .boxed()
                        .toList());
    }

    private <T> List<T> runConcurrent(int threadCount, ThrowingFunction<Integer, T> task) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<T>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                futures.add(executor.submit(() -> {
                    try {
                        startLatch.await();
                        return task.apply(index);
                    } finally {
                        doneLatch.countDown();
                    }
                }));
            }

            startLatch.countDown();

            assertThat(doneLatch.await(10, TimeUnit.SECONDS))
                    .as("All threads should complete within 10 seconds")
                    .isTrue();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                T result = future.get(15, TimeUnit.SECONDS);
                assertThat(result).isNotNull();
                results.add(result);
            }
            return results;
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }

    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
}

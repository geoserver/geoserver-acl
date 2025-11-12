/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.geoserver.acl.api.model.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
class AccesControlListApplicationIT extends AbstractAccesControlListApplicationTest {

    private static final DockerImageName POSTGIS_IMAGE_NAME =
            DockerImageName.parse("imresamu/postgis:15-3.4").asCompatibleSubstituteFor("postgres");

    @Container
    static PostgreSQLContainer postgis = new PostgreSQLContainer(POSTGIS_IMAGE_NAME);

    /** Set up the properties defined in values.yml and used as place-holders in application.yml */
    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("pg.host", () -> postgis.getHost());
        registry.add("pg.port", () -> postgis.getFirstMappedPort());
        registry.add("pg.db", () -> postgis.getDatabaseName());
        registry.add("pg.schema", () -> "acltest");
        registry.add("pg.username", postgis::getUsername);
        registry.add("pg.password", postgis::getPassword);
    }

    /**
     * Test for race condition when multiple threads try to create rules with the same priority
     * via REST API. This uses a real PostgreSQL database via testcontainers.
     * See https://github.com/geoserver/geoserver-acl/issues/84
     */
    @Test
    void testConcurrentRuleCreationWithSamePriorityViaRestAPI() throws Exception {
        loginAsAdmin();

        final long PRIORITY = 60000000L;
        final int THREAD_COUNT = 4;

        // Create a latch to synchronize thread starts for maximum contention
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<ResponseEntity<Rule>>> futures = new ArrayList<>();

        try {
            // Submit all tasks
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int index = i;
                Future<ResponseEntity<Rule>> future = executor.submit(() -> {
                    try {
                        // Wait for all threads to be ready
                        startLatch.await();

                        // Create rule with same priority via REST API
                        String json =
                                """
                                {
                                  "priority": %d,
                                  "access": "ALLOW",
                                  "role": "role%d",
                                  "workspace": "workspace1",
                                  "layer": "layer%d"
                                }
                                """
                                        .formatted(PRIORITY, index, index);

                        return post("/api/rules", json, Rule.class);
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

            // Collect results and verify all succeeded
            List<String> createdRuleIds = new ArrayList<>();
            for (Future<ResponseEntity<Rule>> future : futures) {
                ResponseEntity<Rule> response = future.get(15, TimeUnit.SECONDS);
                assertThat(response.getStatusCode())
                        .as("All requests should succeed")
                        .isEqualTo(HttpStatus.CREATED);
                createdRuleIds.add(response.getBody().getId());
            }

            // Verify all rules were created
            assertThat(createdRuleIds).hasSize(THREAD_COUNT);

            // Re-fetch all rules from the database to get their FINAL priorities
            // (after any shifting that occurred)
            ResponseEntity<Rule[]> allRulesResponse = get("/api/rules", Rule[].class);
            assertThat(allRulesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<Rule> allRules = List.of(allRulesResponse.getBody());
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

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import static org.junit.jupiter.api.Assertions.fail;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.api.client.AdminRulesApi;
import org.geoserver.acl.api.client.config.RepositoryClientAdaptorsConfiguration;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("TODO")
@ActiveProfiles(value = "integration")
@SpringBootTest(
        classes = RepositoryClientAdaptorsConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
@AutoConfigureMockMvc
class AdminRuleRepositoryClientAdaptorTest {

    private AdminRuleRepositoryClientAdaptor repository;

    @BeforeEach
    void setUp() throws Exception {
        AdminRulesApi apiClient;
        AdminRuleApiMapper ruleMapper;
        EnumsApiMapper enumsMapper;
        //        repository = new AdminRuleRepositoryClientAdaptor(apiClient, ruleMapper,
        // enumsMapper);
    }

    @Test
    void testCreate() {
        fail("Not yet implemented");
    }

    @Test
    void testSave() {
        fail("Not yet implemented");
    }

    @Test
    void testFindById() {
        fail("Not yet implemented");
    }

    @Test
    void testFindOne() {
        fail("Not yet implemented");
    }

    @Test
    void testFindAll() {
        fail("Not yet implemented");
    }

    @Test
    void testFindAllAdminRuleFilter() {
        fail("Not yet implemented");
    }

    @Test
    void testFindAllRuleQueryOfAdminRuleFilter() {
        fail("Not yet implemented");
    }

    @Test
    void testFindFirst() {
        fail("Not yet implemented");
    }

    @Test
    void testCount() {
        fail("Not yet implemented");
    }

    @Test
    void testCountAdminRuleFilter() {
        fail("Not yet implemented");
    }

    @Test
    void testShiftPriority() {
        fail("Not yet implemented");
    }

    @Test
    void testSwap() {
        fail("Not yet implemented");
    }

    @Test
    void testDeleteById() {
        fail("Not yet implemented");
    }

    @Test
    void testDelete() {
        fail("Not yet implemented");
    }

    @Test
    void testFindOneByPriority() {
        fail("Not yet implemented");
    }

    @Test
    void testAdminRuleRepositoryClientAdaptor() {
        fail("Not yet implemented");
    }
}

/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.domain.filter.predicate.FilterType;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j(topic = "mapper-test")
class RuleFilterApiMapperTest {

    private RuleFilterApiMapper mapper = new RuleFilterApiMapper();

    private static ObjectMapper objectMapper;

    private RuleFilter filter;

    @BeforeAll
    static void createObjectMapper() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void beforeEach() {
        filter = new RuleFilter();
    }

    @Test
    void testIdName_default() {
        testRoundtrip(filter);
    }

    @Test
    void testRoles_default() {
        filter.getRole().setType(SpecialFilterType.DEFAULT);
        testRoundtrip(filter);
    }

    @Test
    void testRoles_any() {
        filter.getRole().setHeuristically("*");
        assertEquals(FilterType.ANY, filter.getRole().getType());
        testRoundtrip(filter);
    }

    @Test
    void testRoles_single() {
        filter.setRole("r1");
        testRoundtrip(filter);
        filter.getRole().setIncludeDefault(false);
        testRoundtrip(filter);
    }

    @Test
    void testRoles_multiple() {
        filter.setRole("r3,r2,r1");
        testRoundtrip(filter);
        filter.getRole().setIncludeDefault(false);
        testRoundtrip(filter);
    }

    @Test
    void testDefault() {
        RuleFilter model = new RuleFilter();
        testRoundtrip(model);
    }

    @Test
    void testFull() {
        RuleFilter model = new RuleFilter();
        model.setLayer("layer");
        model.getRequest().setHeuristically("*");
        model.setService("service");
        model.setRole("r1,r3,r2");
        model.setSourceAddress("192.168.0.1/32");
        model.setSubfield("sf");
        model.setUser("user");
        model.getUser().setIncludeDefault(false);
        model.setWorkspace("ws");
        testRoundtrip(model);
    }

    private void testRoundtrip(RuleFilter model) {
        org.geoserver.acl.webapi.v1.model.RuleFilter api = mapper.toApi(model);
        RuleFilter roundtripped = mapper.toModel(api);
        print(model, api, roundtripped);
        assertEquals(model.getLayer(), roundtripped.getLayer());
        assertEquals(model.getRequest(), roundtripped.getRequest());
        assertEquals(model.getRole(), roundtripped.getRole());
        assertEquals(model.getService(), roundtripped.getService());
        assertEquals(model.getSourceAddress(), roundtripped.getSourceAddress());
        assertEquals(model.getSubfield(), roundtripped.getSubfield());
        assertEquals(model.getUser(), roundtripped.getUser());
        assertEquals(model.getWorkspace(), roundtripped.getWorkspace());
        assertEquals(model, roundtripped);
    }

    @SneakyThrows(JacksonException.class)
    private void print(RuleFilter model, org.geoserver.acl.webapi.v1.model.RuleFilter api, RuleFilter roundtripped) {
        log.debug("model: {}", model);
        log.debug("api  : {}", objectMapper.writeValueAsString(api));
        log.debug("rtp  : {}", roundtripped);
    }

    @SneakyThrows(JacksonException.class)
    void print(org.geoserver.acl.webapi.v1.model.RuleFilter api) {
        String encoded = objectMapper.writeValueAsString(api);
        System.err.println(encoded);
    }
}

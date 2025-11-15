/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.jndi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test suite for {@link SimpleNamingContextBuilder}
 *
 * @since 1.0
 */
class SimpleNamingContextBuilderTest {

    @BeforeEach
    void beforeEach() {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
    }

    @Test
    void testInitialContext() throws NamingException {
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        NamingManager.setInitialContextFactoryBuilder(builder);
        assertTrue(NamingManager.hasInitialContextFactoryBuilder());
        Context context = NamingManager.getInitialContext(new Hashtable<>());
        assertThat(context).isInstanceOf(SimpleNamingContext.class);
    }

    @Test
    void testNewInitialContext() throws NamingException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, SimpleNamingContextFactory.class.getName());
        InitialContext ctx = new InitialContext();
        Context subcontext = ctx.createSubcontext("java:comp");
        assertThat(subcontext).isInstanceOf(SimpleNamingContext.class);
    }
}

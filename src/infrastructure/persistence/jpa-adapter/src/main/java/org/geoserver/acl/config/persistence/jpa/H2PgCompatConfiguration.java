/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.persistence.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * H2 compatibility configuration that provides a transaction-scoped advisory lock implementation
 * equivalent to PostgreSQL's {@code pg_advisory_xact_lock(bigint)}.
 *
 * <p>On startup, detects H2 and registers a {@code CREATE ALIAS} pointing to
 * {@link #pg_advisory_xact_lock(long)}. On PostgreSQL, this configuration is a no-op - the native
 * function is used directly.
 *
 * <p>The lock uses a per-key {@link ReentrantLock} that is acquired when the SQL function is called
 * and released automatically when the Spring-managed transaction completes (commit or rollback),
 * mirroring PostgreSQL's transaction-scoped advisory lock semantics, and enabling the dev profile
 * (H2) and H2-based integration tests to correctly serialize concurrent priority modifications.
 *
 * @since 3.0
 */
@Configuration(proxyBeanMethods = false)
public class H2PgCompatConfiguration {

    private static final ConcurrentHashMap<Long, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    /**
     * Creates a no-op {@code pg_advisory_xact_lock} function on H2 so the advisory lock calls in
     * the JPA adaptors work transparently. On PostgreSQL the native function is used. Safe to call
     * on any database - only executes the DDL when H2 is detected.
     */
    @Bean
    SmartInitializingSingleton h2PgAdvisoryLockCompat(@Qualifier("authorizationDataSource") DataSource dataSource) {
        return () -> {
            try (Connection conn = dataSource.getConnection()) {
                String productName = conn.getMetaData().getDatabaseProductName();
                if ("H2".equalsIgnoreCase(productName)) {
                    String className = H2PgCompatConfiguration.class.getName();
                    conn.createStatement()
                            .execute(
                                    """
                                    CREATE ALIAS IF NOT EXISTS pg_advisory_xact_lock \
                                    FOR '%s.pg_advisory_xact_lock'
                                    """
                                            .formatted(className));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to initialize H2 pg_advisory_xact_lock compatibility", e);
            }
        };
    }

    /** Called by H2 when executing {@code SELECT pg_advisory_xact_lock(?)}. */
    @SuppressWarnings("java:S100") // method name
    public static void pg_advisory_xact_lock(long key) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            lock(key);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    unlock(key);
                }
            });
        }
    }

    private static void unlock(long key) {
        ReentrantLock lock = LOCKS.get(key);
        if (lock != null) {
            lock.unlock();
        }
    }

    @SuppressWarnings("java:S2222") // not releasing lock here
    private static void lock(long key) {
        ReentrantLock lock = LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for H2 advisory lock (key=" + key + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for H2 advisory lock", e);
        }
    }
}

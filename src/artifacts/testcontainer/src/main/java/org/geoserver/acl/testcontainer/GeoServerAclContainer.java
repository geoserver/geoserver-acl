/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.testcontainer;

import static org.junit.Assume.assumeTrue;

import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;
import org.junit.Assume;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class GeoServerAclContainer extends GenericContainer<GeoServerAclContainer> {

    private static final WaitStrategy ACTUATOR_READINESS_PROBE =
            Wait.forHttp("/actuator/health/readiness").forPort(8081).withStartupTimeout(Duration.ofSeconds(10));

    public static final String DEFAULT_IMAGE_REPOSITORY = "geoservercloud/geoserver-acl";

    public static final DockerImageName V1_0_0 = DockerImageName.parse(DEFAULT_IMAGE_REPOSITORY + ":1.0.0");

    public static final DockerImageName V1_1_0 = DockerImageName.parse(DEFAULT_IMAGE_REPOSITORY + ":1.1.0");

    public static final DockerImageName V1_2_0 = DockerImageName.parse(DEFAULT_IMAGE_REPOSITORY + ":1.2.0");

    public static final String CURRENT_VERSION =
            Objects.requireNonNull(GeoServerAclContainer.class.getPackage().getImplementationVersion());

    public static final DockerImageName CURRENT =
            DockerImageName.parse(DEFAULT_IMAGE_REPOSITORY + ":" + CURRENT_VERSION);

    /** flag for {@link #disabledWithoutDocker()} */
    private boolean disabledWithoutDocker;

    public GeoServerAclContainer() {
        this(CURRENT);
    }

    public GeoServerAclContainer(DockerImageName image) {
        super(image);
        super.withExposedPorts(8080, 8081);
        super.setWaitStrategy(ACTUATOR_READINESS_PROBE);
    }

    public static GeoServerAclContainer currentVersion() {
        return new GeoServerAclContainer();
    }

    public GeoServerAclContainer withDevMode() {
        super.withEnv("SPRING_PROFILES_ACTIVE", "dev");
        return this;
    }

    /**
     * Disables the tests using this testcontainer if there's no Docker environment available.
     *
     * <p>Same effect as JUnit 5's {@code
     * org.testcontainers.junit.jupiter.@Testcontainers(disabledWithoutDocker = true)}
     */
    public GeoServerAclContainer disabledWithoutDocker() {
        this.disabledWithoutDocker = true;
        return this;
    }

    /**
     * Support for JUnit 4 to have the same effect as JUnit 5's {@code
     * org.testcontainers.junit.jupiter.@Testcontainers(disabledWithoutDocker = true)} when {@link
     * #disabledWithoutDocker()}.
     *
     * <p>Overrides to apply the {@link Assume assumption} checking the Docker environment is
     * available if {@link #disabledWithoutDocker() enabled}, so this test container can be used as
     * a {@code ClassRule @ClassRule} and hence avoid running a container for each test case.
     */
    @Override
    @SuppressWarnings("deprecation")
    public Statement apply(Statement base, Description description) {
        if (disabledWithoutDocker) {
            assumeTrue(
                    "Docker environment unavailable, ignoring tests",
                    DockerClientFactory.instance().isDockerAvailable());
        }
        return super.apply(base, description);
    }

    @Override
    protected void doStart() {
        Logger.getLogger(getClass().getName()).info("Starting " + getDockerImageName() + " test container");
        super.doStart();
    }

    public String devAdminUser() {
        return "admin";
    }

    public String devAdminPassword() {
        return "s3cr3t";
    }

    public int apiPort() {
        return getMappedPort(8080);
    }

    public String apiUrl() {
        String host = super.getHost();
        int port = apiPort();
        return String.format("http://%s:%d/acl/api", host, port);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}

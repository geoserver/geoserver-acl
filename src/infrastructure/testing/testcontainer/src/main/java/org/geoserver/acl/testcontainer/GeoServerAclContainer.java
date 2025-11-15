/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.testcontainer;

import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;
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
        return "http://%s:%d/acl/api".formatted(host, port);
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

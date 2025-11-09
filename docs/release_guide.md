# Release Guide

This document describes the release process for GeoServer ACL.

## Overview

GeoServer ACL uses a CI-friendly release process where versions are determined by git tags rather than requiring manual updates to `pom.xml` files. The release process involves:

1. Creating and pushing a git tag
2. GitHub Actions automatically builds and publishes the Docker image
3. Jenkins job publishes JAR artifacts to OSGeo repository

## Prerequisites

- Write access to the GitHub repository
- Access to Jenkins at build.geoserver.org
- Docker Hub credentials configured in GitHub secrets
- Maven credentials for OSGeo repository configured in Jenkins

## Release Process

### 1. Prepare the Release

Ensure the `main` branch is in a releasable state:

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Run tests locally
make test

# Verify the build
make package
```

### 2. Create and Push the Release Tag

Create a git tag with the release version:

```bash
# Create the tag (e.g., 3.0.0)
git tag 3.0.0

# Push the tag to GitHub
git push origin 3.0.0
```

**Note:** The tag name will be used as the version number. Use semantic versioning (e.g., `2.9.0`, `3.0.0-RC1`).

### 3. Docker Image Release (Automated)

GitHub Actions will automatically:
- Detect the new tag
- Extract the version from the tag name
- Build the Docker image for `linux/amd64` and `linux/arm64`
- Push to Docker Hub as `geoservercloud/geoserver-acl:<version>`

Monitor the build at: https://github.com/geoserver/geoserver-acl/actions

### 4. Maven Artifacts Release (Jenkins)

Trigger the Jenkins job at build.geoserver.org:

1. Navigate to the GeoServer ACL `geoserver-acl-release` job
2. Click "Build with Parameters"
3. Enter the tag name in the `TAG` parameter (e.g., `3.0.0`)
4. Click "Build"

The Jenkins job will:
- Checkout `refs/tags/${TAG}`
- Build and deploy artifacts to OSGeo releases repository
- Artifacts will be versioned according to the tag name

### 5. Verify the Release

After both processes complete, verify:

**Docker Image:**
```bash
docker pull geoservercloud/geoserver-acl:3.0.0
```

**Maven Artifacts:**
Check the OSGeo releases repository:
- https://repo.osgeo.org/repository/Geoserver-releases/

### 6. Update Documentation

After a successful release:
- Update the changelog (if applicable)
- Update version references in documentation
- Announce the release

## Snapshot Releases

For snapshot releases, simply push to the `main` branch:

```bash
git push origin main
```

This will:
- Build Docker image tagged as `geoservercloud/geoserver-acl:3.0-SNAPSHOT`
- Use the version defined in `pom.xml` (`${revision}` property)

## Version Information

To check the current version:

```bash
# From the Makefile
make show-version

# From Maven directly
./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout
```

## Troubleshooting

**Problem:** Jenkins build fails with version mismatch

**Solution:** Ensure the TAG parameter matches an existing git tag exactly

---

**Problem:** Docker image push fails

**Solution:** Check GitHub secrets for Docker Hub credentials (`DOCKER_HUB_USERNAME` and `DOCKER_HUB_ACCESS_TOKEN`)

---

**Problem:** Maven deploy fails with authentication error

**Solution:** Verify `$MAVEN_SETTINGS` environment variable is configured in Jenkins with correct OSGeo repository credentials

## Release Repositories

- **Docker Images:** https://hub.docker.com/r/geoservercloud/geoserver-acl
- **Maven Releases:** https://repo.osgeo.org/repository/Geoserver-releases/
- **Maven Snapshots:** https://repo.osgeo.org/repository/geoserver-snapshots/


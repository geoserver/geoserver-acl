# Build Instructions

GeoServer ACL uses Maven for dependency management and Make for standardized build workflows.

## Tools Required
*   JDK 17+
*   Maven 3.9+
*   Docker (for integration tests and container builds)

## Quick Start (Makefile)

The project includes a `Makefile` to simplify common operations.

| Command | Description |
| :--- | :--- |
| `make install` | Full build (clean, install, test). |
| `make build-image` | Builds the ACL Service Docker image. |
| `make test` | Runs unit and integration tests. |
| `make format` | Applies code formatting (Palantir Java Format). |
| `make clean` | Cleans target directories. |

## Maven Commands

If you prefer using Maven directly or need to pass specific flags:

### Full Build
```bash
./mvnw clean install
```

### Skip Tests
```bash
./mvnw clean install -DskipTests
```

### Build GeoServer Plugin Only
The plugin must be built against Java 11 source compatibility.
```bash
./mvnw clean install -pl src/plugin -Pgs-plugin
```

### Build Docker Image
```bash
./mvnw clean package -pl src/application/authorization-app -Pdocker -DskipTests
```

## Release Process

Releases are automated via GitHub Actions, but can be performed locally if necessary:

1.  **Version Bump**:
    ```bash
    ./mvnw versions:set -DnewVersion=1.0.0
    ./mvnw versions:commit
    ```
2.  **Verify Build**:
    ```bash
    make install
    ```
3.  **Tag & Push**:
    ```bash
    git commit -am "Release 1.0.0"
    git tag v1.0.0
    git push origin v1.0.0
    ```

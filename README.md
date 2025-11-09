![build](https://github.com/geoserver/geoserver-acl/actions/workflows/build.yaml/badge.svg)

# GeoServer Access Control List (ACL)

GeoServer ACL is an advanced authorization system for [GeoServer](https://geoserver.org/),
providing fine-grained access control to secure your geospatial data and services.

It consists of an independent application service that manages access rules,
and a GeoServer [plugin](https://docs.geoserver.org/latest/en/user/community/acl/index.html)
that requests authorization limits on a per-request basis.
For [GeoServer Cloud](https://github.com/geoserver/geoserver-cloud), the ACL plugin
is integrated by default.

## Features

GeoServer ACL provides two types of rules:

**Data Access Rules** - Control what data users can access:
- Workspace and layer access control (allow, deny, or limit access)
- Service-level filtering (WMS, WFS, WCS, etc.)
- Geographic filtering (restrict to specific areas)
- Attribute-level security (control access to specific layer attributes)
- CQL filters for read and write operations
- Style restrictions

**Admin Rules** - Control administrative privileges:
- Workspace administration rights (manage stores, layers, and styles)
- Role and user-based administrative access

Both rule types support:
- User and role-based authorization
- IP address filtering
- Priority-based rule ordering for flexible access control

## How It Works

Administrators define two types of rules: **data access rules** that control what data users can access,
and **admin rules** that determine who has administrative privileges on workspaces. On each GeoServer
request, the ACL service receives an authorization request with the user's credentials (username, roles,
IP address) and the target resource, then matches these against the defined rules to grant or deny access.

GeoServer ACL is not an authentication provider. It's an authorization manager that uses
authenticated user credentials (from Basic HTTP, OAuth2/OpenID Connect, or any other
authentication mechanism GeoServer supports) to make authorization decisions.

GeoServer ACL is Open Source, born as a
[fork](https://en.wikipedia.org/wiki/Fork_%28software_development%29) of
[GeoFence](https://github.com/geoserver/geofence).
As such, it follows the same logic to define data access and administrative
access rules. So if you're familiar with GeoFence, it'll be easy to reason
about GeoServer ACL.

## Quick Start

Docker images are available at [Docker Hub](https://hub.docker.com/r/geoservercloud/geoserver-acl).

### Running with Docker

```bash
# Run with in-memory H2 database (development/testing only)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  geoservercloud/geoserver-acl:3.0.0
```

The service will be available at:
- Base URL: http://localhost:8080/acl
- Swagger UI: http://localhost:8080/acl/openapi/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/acl/openapi

**Note:** The H2 in-memory database is only suitable for development and testing. For production deployments, use PostgreSQL.

### Running with Docker Compose (Recommended)

Create a `docker-compose.yml` file:

```yaml
volumes:
  acl_data:

services:
  acldb:
    image: imresamu/postgis:15-3.4
    environment:
      - POSTGRES_DB=acl
      - POSTGRES_USER=acl
      - POSTGRES_PASSWORD=acls3cr3t
    volumes:
      - acl_data:/var/lib/postgresql/data
    restart: always
    ports:
      - 6432:5432
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U acl"]
      interval: 5s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 2G

  acl:
    image: geoservercloud/geoserver-acl:${TAG:-3.0.0}
    environment:
      - PG_HOST=acldb
      - PG_PORT=5432
      - PG_DB=acl
      - PG_SCHEMA=acl
      - PG_USER=acl
      - PG_PASSWORD=acls3cr3t
    ports:
      - 8080:8080
    depends_on:
      acldb:
        condition: service_healthy
        required: true
```

Then run:

```bash
docker compose up -d
```

## Configuration

GeoServer ACL can be configured using environment variables. All configuration properties can be overridden using uppercase environment variables with underscores (e.g., `pg.host` → `PG_HOST`).

### Key Environment Variables

#### PostgreSQL Database

| Variable | Description |
|----------|-------------|
| `PG_HOST` | PostgreSQL hostname |
| `PG_PORT` | PostgreSQL port |
| `PG_DB` | Database name |
| `PG_SCHEMA` | Database schema |
| `PG_USER` | Database username |
| `PG_PASSWORD` | Database password |

All PostgreSQL connection parameters must be configured by the user.

#### Security

| Variable | Description | Default |
|----------|-------------|---------|
| `ACL_USERS_ADMIN_PASSWORD` | Admin user password | *Change in production* |
| `ACL_USERS_GEOSERVER_PASSWORD` | GeoServer user password | *Change in production* |
| `ACL_SECURITY_HEADERS_ENABLED` | Enable header-based authentication | `false` |

**Security Note:** Always override default passwords in production using environment variables or Docker secrets.

#### Application

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP port | `8080` |
| `ACL_CACHING` | Enable ACL rule caching | `true` |

For a complete list of configuration options, see the [documentation](https://geoserver.org/geoserver-acl/).

## API Documentation

GeoServer ACL exposes a REST API for managing access rules:

- **Swagger UI:** `http://localhost:8080/acl/openapi/swagger-ui/index.html`
- **OpenAPI spec:** `http://localhost:8080/acl/openapi`

The API allows you to manage access rules for workspaces, layers, services, and users, including geographic and attribute-level filtering.

## Building from Source

**Requirements:**
- Java 17 JDK
- Maven 3.9+

Build the project:

```bash
./mvnw clean install
```

Run tests:

```bash
./mvnw test
```

Build Docker image:

```bash
make build-image
```

The project uses a [Makefile](Makefile) with additional targets for linting, formatting, and packaging. See `make help` for all available commands.


## Documentation

For more detailed information:

- [GeoServer ACL Documentation](https://geoserver.org/geoserver-acl/) - Official documentation
- [GeoServer Cloud Documentation](https://geoserver.org/geoserver-cloud/) - GeoServer Cloud integration details
- [API Documentation](https://geoserver.org/geoserver-acl/api/) - REST API reference

## Contributing

Please read the [contribution guidelines](CONTRIBUTING.md) before contributing pull requests to the GeoServer ACL project.

## Related Projects

- [GeoServer](https://geoserver.org/) - Open source server for geospatial data
- [GeoServer Cloud](https://github.com/geoserver/geoserver-cloud) - Cloud-native GeoServer deployment (includes ACL integration by default)
- [GeoFence](https://github.com/geoserver/geofence) - The original project that inspired GeoServer ACL

## License

GeoServer ACL is Open Source software licensed under the GNU General Public License v2.0.

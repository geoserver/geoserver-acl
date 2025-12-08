# Configuration

GeoServer ACL is configured via Spring Boot configuration files (`application.yml`) or environment variables. This page details the available configuration properties for the ACL Service and the GeoServer Plugin.

## ACL Service Configuration

The ACL Service (`geoserver-acl-app`) requires a database connection and basic server settings.

### Database Connection
Configure the connection to the PostgreSQL/PostGIS database.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/acldb
    username: acl
    password: acl_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### Core ACL Settings

| Property | Default | Description |
| :--- | :--- | :--- |
| `geoserver.acl.security.enabled` | `true` | Enables or disables the security subsystem. |
| `geoserver.acl.security.admin-role` | `ADMIN` | The role name that grants full access to the ACL API. |
| `geoserver.acl.default-deny` | `true` | If `true`, requests matching no rules are denied. |
| `geoserver.acl.cache.enabled` | `true` | Enables caching of authorization decisions. |
| `geoserver.acl.cache.expiration` | `1h` | Duration before cached decisions expire. |

### Authentication Modes

#### 1. Internal Authentication (Default)
Uses the internal ACL database to store users and passwords.

```yaml
geoserver:
  acl:
    security:
      internal:
        enabled: true
```

#### 2. Pre-Authentication (Gateway/Proxy)
Trusts headers from an upstream identity provider (e.g., OAuth2 Proxy).

```yaml
geoserver:
  acl:
    security:
      preauth:
        enabled: true
        user-header: X-Forwarded-User
        roles-header: X-Forwarded-Roles
```

## GeoServer Plugin Configuration

The plugin running inside GeoServer must be configured to talk to the ACL Service. This is done via the **GeoServer Admin UI** > **Security** > **GeoServer ACL**.

### Connection Settings

| Setting | Description |
| :--- | :--- |
| **ACL Service URL** | The endpoint of the ACL service (e.g., `http://acl:8080/acl`). |
| **Connection Timeout** | Max time (ms) to wait for a connection to the ACL service. |
| **Read Timeout** | Max time (ms) to wait for a response. |
| **Cache Enabled** | Enables client-side caching of rules within GeoServer to reduce network calls. |

## Environment Variables

For Docker deployments, map these environment variables to the container.

| Variable | Maps To |
| :--- | :--- |
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` |
| `ACL_ADMIN_ROLE` | `geoserver.acl.security.admin-role` |

## Example: Production `application.yml`

```yaml
server:
  port: 8080
  tomcat:
    max-threads: 200

spring:
  datasource:
    url: jdbc:postgresql://db.prod:5432/acldb
    username: ${DB_USER}
    password: ${DB_PASS}
  cloud:
    bus: # Enable for multi-instance cache invalidation
      enabled: true
      destination: acl-events

geoserver:
  acl:
    security:
      admin-role: ROLE_SUPERUSER
      preauth:
        enabled: true
    cache:
      max-size: 50000
      expiration: 30m
```

# API Reference

The GeoServer ACL Service exposes a RESTful API for rule management and authorization.

**Base URL**: `http://<host>:8080/acl`
**OpenAPI Spec**: `/api-docs`
**Swagger UI**: `/swagger-ui.html`

## Authentication

The API supports two primary authentication modes:

### 1. Basic Authentication
Standard HTTP Basic Auth using the configured admin credentials.

```http
Authorization: Basic YWRtaW46Z2Vvc2VydmVy
```

### 2. Pre-Authentication (Headers)
When running behind a trusted gateway (e.g., OAuth2 Proxy), the service trusts identity headers.

*   `X-Forwarded-User`: The username.
*   `X-Forwarded-Roles`: Comma-separated list of roles.

## Core Endpoints

### 1. Rule Management (`/api/rules`)

#### Create Rule
**POST** `/api/rules`

```json
{
  "priority": 100,
  "access": "ALLOW",
  "userName": "jsmith",
  "workspace": "public",
  "layer": "roads",
  "service": "WMS"
}
```

#### List Rules
**GET** `/api/rules`

*   Query Params: `page`, `size`, `sort`

#### Delete Rule
**DELETE** `/api/rules/{id}`

### 2. Admin Rule Management (`/api/adminrules`)

#### Grant Workspace Admin
**POST** `/api/adminrules`

```json
{
  "priority": 50,
  "access": "ADMIN",
  "userName": "city_manager",
  "workspace": "city_data"
}
```

### 3. Authorization Check (`/api/authorization`)

Test if a user has access to a resource.

**POST** `/api/authorization`

```json
{
  "user": "jsmith",
  "roles": ["ROLE_USER"],
  "workspace": "public",
  "layer": "roads",
  "service": "WMS",
  "request": "GetMap"
}
```

**Response**:
```json
{
  "grant": true,
  "ruleLimits": null,
  "layerDetails": null
}
```

## Batch Operations

For bulk migration or backup/restore.

#### Import Rules
**POST** `/api/rules/batch`

Accepts a JSON array of Rule objects.

```json
[
  { "priority": 10, "access": "DENY", "workspace": "secure" },
  { "priority": 20, "access": "ALLOW", "workspace": "public" }
]
```
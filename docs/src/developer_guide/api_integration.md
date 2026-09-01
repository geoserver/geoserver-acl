# API Reference

The GeoServer ACL Service exposes a RESTful API for rule management and authorization.

**Base URL**: `http://<host>:8080/acl`
**API base path**: `/acl/api`
**OpenAPI Spec**: `/acl/openapi`
**Swagger UI**: `/acl/openapi/swagger-ui/index.html`

## Authentication

The API supports two primary authentication modes:

### 1. Basic Authentication
Standard HTTP Basic Auth using the configured admin credentials.

```http
Authorization: Basic YWRtaW46Z2Vvc2VydmVy
```

### 2. Pre-Authentication (Headers)
When running behind a trusted gateway, the service can trust identity headers. This mode is disabled by default (`acl.security.headers.enabled`), and the header names are configurable:

*   `sec-username`: The username (`acl.security.headers.user-header`).
*   `sec-roles`: Comma-separated list of roles (`acl.security.headers.roles-header`).

Requests presenting a role listed in `acl.security.headers.admin-roles` (default `ROLE_ADMINISTRATOR`) get administrative access.

## Core Endpoints

### 1. Rule Management (`/api/rules`)

#### Create Rule
**POST** `/api/rules`

```json
{
  "priority": 100,
  "access": "ALLOW",
  "user": "jsmith",
  "workspace": "public",
  "layer": "roads",
  "service": "WMS"
}
```

#### List Rules
**GET** `/api/rules`

*   Query Params: `limit`, `nextCursor` (cursor-based pagination)

#### Update Rule
**PATCH** `/api/rules/id/{id}`

#### Delete Rule
**DELETE** `/api/rules/id/{id}`

### 2. Admin Rule Management (`/api/adminrules`)

#### Grant Workspace Admin
**POST** `/api/adminrules`

```json
{
  "priority": 50,
  "access": "ADMIN",
  "user": "city_manager",
  "workspace": "city_data"
}
```

### 3. Authorization (`/api/authorization`)

Evaluate what access a request gets. Fields left absent match any value; the domain rejects `*` in authorization requests.

**POST** `/api/authorization/resources`

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

**Response** (`AccessInfo`):
```json
{
  "grant": "ALLOW",
  "catalogMode": null,
  "defaultStyle": null,
  "allowedStyles": null,
  "cqlFilterRead": null,
  "cqlFilterWrite": null,
  "matchingRules": ["<rule-id>"]
}
```

Related endpoints:

*   **POST /api/authorization/admin**: Evaluate workspace administration access (`AdminAccessRequest` -> `AdminAccessInfo`).
*   **POST /api/authorization/resources/matchingrules**: Return the rules that match an access request.
*   **POST /api/authorization/accesssummary**: Summarize a user's access across workspaces.

## Bulk Queries

There is no bulk import endpoint; create rules individually with `POST /api/rules`. For querying at scale, use the filter endpoints with cursor-based pagination:

*   **POST /api/rules/query**: Search rules matching a `RuleFilter`.
*   **POST /api/rules/query/count**: Count rules matching a `RuleFilter`.

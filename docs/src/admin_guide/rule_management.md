# Data Access Rules

Data Access Rules define the authorization policy for standard OGC service requests. This reference details the rule structure and management API.

## Rule Specification

A Rule is a JSON object with the following schema:

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `priority` | Integer | **Yes** | Evaluation order (0 is highest priority). Unique per rule. |
| `access` | Enum | **Yes** | `ALLOW`, `DENY`, or `LIMIT`. |
| `roleName` | String | No | The role this rule applies to. (Use `*` for all roles). |
| `userName` | String | No | The specific user this rule applies to. (Use `*` for all users). |
| `service` | String | No | OGC Service (e.g., `WMS`, `WFS`). `*` matches all. |
| `request` | String | No | Service operation (e.g., `GetMap`). `*` matches all. |
| `workspace` | String | No | GeoServer workspace name. `*` matches all. |
| `layer` | String | No | Layer name. `*` matches all. |
| `addressRange` | CIDR | No | IP address range (e.g., `192.168.1.0/24`). |
| `ruleLimits` | Object | No | Spatial restrictions (see below). |
| `layerDetails` | Object | No | Attribute/Catalog restrictions (see below). |

**Note**: At least one of `roleName` or `userName` must be specified. Use `*` to match everyone.

## Rule Evaluation Logic

1.  **Filter**: The engine selects all rules where the request parameters match the rule definition (e.g., matching user, workspace, service).
2.  **Sort**: Matching rules are sorted by `priority` (ascending).
3.  **Apply**: The first matching rule determines the outcome.
4.  **Default**: If no rule matches, the global default (usually `DENY`) applies.

## JSON Examples

### 1. Public Read-Only Access
Allows anonymous access (`*`) to view maps (`WMS`) but denies data download (`WFS`).

```json
[
  {
    "priority": 1000,
    "access": "ALLOW",
    "roleName": "*",
    "workspace": "public",
    "service": "WMS"
  },
  {
    "priority": 1001,
    "access": "DENY",
    "roleName": "*",
    "workspace": "public",
    "service": "WFS"
  }
]
```

### 2. Spatially Restricted User
Restricts a specific user to a polygon area.

```json
{
  "priority": 100,
  "access": "LIMIT",
  "userName": "contractor_1",
  "workspace": "project_a",
  "layer": "site_boundary",
  "ruleLimits": {
    "allowedArea": "POLYGON((...))",
    "spatialFilterType": "INTERSECT"
  }
}
```

### 3. Attribute Masking
Hides sensitive columns from a layer.

```json
{
  "priority": 50,
  "access": "LIMIT",
  "roleName": "ROLE_INTERNAL",
  "workspace": "hr",
  "layer": "employees",
  "layerDetails": {
    "attributes": {
      "excludedAttributes": ["salary", "ssn"],
      "accessType": "READONLY"
    }
  }
}
```

## Management API

Rules are managed via the REST API at `/api/rules`.

*   **GET /api/rules**: List all rules (supports pagination).
*   **POST /api/rules**: Create a new rule.
*   **PUT /api/rules/{id}**: Update an existing rule.
*   **DELETE /api/rules/{id}**: Delete a rule.

### Bulk Operations
For migration or backup/restore:

*   **POST /api/rules/batch**: Accepts a JSON array of rule objects.

## Priority Strategy

To maintain a manageable rule set:

1.  **Reserve Ranges**: Use ranges for different scopes (e.g., 0-99 for User Overrides, 100-999 for Layer Rules, 1000+ for Global Defaults).
2.  **Deny First**: Place high-priority specific DENY rules (e.g., "Deny restricted_layer to everyone") before broad ALLOW rules.
3.  **Gaps**: Leave priority gaps (e.g., increment by 10) to allow inserting rules later without re-indexing.

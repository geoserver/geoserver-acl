# Data Access Rules

Data Access Rules define the authorization policy for standard OGC service requests. This reference details the rule structure and management API.

## Rule Specification

A Rule is a JSON object with the following schema:

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `priority` | Integer | **Yes** | Evaluation order. Lower values are evaluated first. Unique per rule. |
| `access` | Enum | **Yes** | `ALLOW`, `DENY`, or `LIMIT`. |
| `role` | String | No | The role this rule applies to. When absent, the rule applies to any role. |
| `user` | String | No | The specific user this rule applies to. When absent, the rule applies to any user. |
| `service` | String | No | OGC Service (e.g., `WMS`, `WFS`). When absent, any service matches. |
| `request` | String | No | Service operation (e.g., `GetMap`). When absent, any operation matches. |
| `subfield` | String | No | Operation-specific qualifier. When absent, any subfield matches. |
| `workspace` | String | No | GeoServer workspace name. When absent, any workspace matches. |
| `layer` | String | No | Layer name. When absent, any layer matches. |
| `addressRange` | CIDR | No | IP address range (e.g., `192.168.1.0/24`). |
| `limits` | Object | No | Spatial restrictions for `LIMIT` rules (see below). |

**Note**: All matching fields are optional; a rule with none of them matches every request. To target anonymous requests, use the role that identifies them, `ROLE_ANONYMOUS` by GeoServer convention. The `*` wildcard belongs to the rule query filters (see [Filtering](filtering.md)), not to rule definitions.

Attribute and catalog-mode restrictions (`layerDetails`) are managed through a dedicated endpoint after the rule is created (see below).

## Rule Evaluation Logic

1.  **Filter**: The engine selects all rules where the request parameters match the rule definition (e.g., matching user, workspace, service).
2.  **Sort**: Matching rules are sorted by `priority` (ascending).
3.  **Apply**: The first matching rule determines the outcome.
4.  **Default**: If no rule matches, access is denied.

## JSON Examples

### 1. Public Read-Only Access
Allows everyone to view maps (`WMS`) but denies data download (`WFS`). The rules omit `role` and `user`, matching any principal.

```json
[
  {
    "priority": 1000,
    "access": "ALLOW",
    "workspace": "public",
    "service": "WMS"
  },
  {
    "priority": 1001,
    "access": "DENY",
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
  "user": "contractor_1",
  "workspace": "project_a",
  "layer": "site_boundary",
  "limits": {
    "allowedArea": { "wkt": "SRID=4326;POLYGON((...))" },
    "spatialFilterType": "INTERSECT"
  }
}
```

### 3. Attribute Masking
Hides sensitive columns from a layer. First create the `LIMIT` rule:

```json
{
  "priority": 50,
  "access": "LIMIT",
  "role": "ROLE_INTERNAL",
  "workspace": "hr",
  "layer": "employees"
}
```

Then set its layer details with `PUT /api/rules/id/{id}/layer-details`:

```json
{
  "layerAttributes": [
    { "name": "salary", "access": "NONE" },
    { "name": "ssn", "access": "NONE" },
    { "name": "name", "access": "READONLY" }
  ]
}
```

## Management API

Rules are managed via the REST API under `/api/rules`.

*   **GET /api/rules**: List all rules in priority order. Cursor-based pagination via the `limit` and `nextCursor` parameters.
*   **POST /api/rules**: Create a new rule. The optional `position` parameter controls how `priority` is interpreted (`FIXED`, `FROM_START`, `FROM_END`).
*   **PATCH /api/rules/id/{id}**: Update an existing rule.
*   **DELETE /api/rules/id/{id}**: Delete a rule.
*   **DELETE /api/rules**: Delete all rules, returning the removed count.
*   **POST /api/rules/query**: Search rules with a filter (see [Filtering](filtering.md)).
*   **GET /api/rules/query/count** and **POST /api/rules/query/count**: Count all rules, or the rules matching a filter.
*   **PUT /api/rules/id/{id}/limits**, **PUT /api/rules/id/{id}/styles**, **PUT /api/rules/id/{id}/layer-details**: Set the rule's spatial limits, allowed styles, and layer details.
*   **POST /api/rules/shift**, **POST /api/rules/id/{id}/swapwith/{id2}**: Re-prioritize rules.

## Priority Strategy

To maintain a manageable rule set:

1.  **Reserve Ranges**: Use ranges for different scopes (e.g., 0-99 for User Overrides, 100-999 for Layer Rules, 1000+ for Global Defaults).
2.  **Deny First**: Place high-priority specific DENY rules (e.g., "Deny restricted_layer to everyone") before broad ALLOW rules.
3.  **Gaps**: Leave priority gaps (e.g., increment by 10) to allow inserting rules later without re-indexing.

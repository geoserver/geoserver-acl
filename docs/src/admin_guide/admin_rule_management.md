# Admin Rules

Admin Rules control access to GeoServer's administrative functions (Web UI and REST API). They allow for delegated administration of specific workspaces.

## Rule Specification

An Admin Rule is a JSON object with the following schema:

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `priority` | Integer | **Yes** | Evaluation order (0 is highest priority). Unique per rule. |
| `access` | Enum | **Yes** | `ADMIN`, `USER`, or `GROUP`. |
| `roleName` | String | No | The role this rule applies to. (Use `*` for all roles). |
| `userName` | String | No | The specific user this rule applies to. (Use `*` for all users). |
| `workspace` | String | **Yes** | The workspace scope for the permissions. `*` matches all workspaces. |
| `addressRange` | CIDR | No | IP address range restriction. |

### Access Levels

*   **ADMIN**: Grants full administrative privileges within the scope.
    *   If `workspace` is `*`: User is a global administrator.
    *   If `workspace` is specific (e.g., `topp`): User is a Workspace Administrator for `topp`.
*   **USER**: Grants read-only access to the administrative configuration.
*   **GROUP**: Grants permission to manage users within a specific group (requires Group Service configuration).

## JSON Examples

### 1. Global System Administrator
Grants full control over the entire GeoServer instance.

```json
{
  "priority": 0,
  "access": "ADMIN",
  "roleName": "ROLE_SYSADMIN",
  "workspace": "*"
}
```

### 2. Workspace Administrator
Allows a user to manage data, layers, and styles *only* within the `engineering` workspace.

```json
{
  "priority": 100,
  "access": "ADMIN",
  "userName": "eng_lead",
  "workspace": "engineering"
}
```

### 3. Read-Only Auditor
Allows an auditor to view configurations for all workspaces but change nothing.

```json
{
  "priority": 500,
  "access": "USER",
  "roleName": "ROLE_AUDITOR",
  "workspace": "*"
}
```

## Management API

Admin Rules are managed via the REST API at `/api/adminrules`.

*   **GET /api/adminrules**: List all admin rules.
*   **POST /api/adminrules**: Create a new admin rule.
*   **PUT /api/adminrules/{id}**: Update an existing admin rule.
*   **DELETE /api/adminrules/{id}**: Delete an admin rule.

## Differences from Data Rules

It is critical to distinguish between **Admin Rules** and **Data Rules**:

*   **Admin Rules** control the **configuration** (Can I add a layer? Can I change a style?).
*   **Data Rules** control the **content** (Can I see the map? Can I query the database?).

A user may have `ADMIN` rights to a workspace (via Admin Rule) but be denied viewing the actual map data if a specific `DENY` Data Rule exists for them.
# Admin Rules

Admin Rules control access to GeoServer's administrative functions (Web UI and REST API). They allow for delegated administration of specific workspaces.

## Rule Specification

An Admin Rule is a JSON object with the following schema:

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `priority` | Integer | **Yes** | Evaluation order. Lower values are evaluated first. Unique per rule. |
| `access` | Enum | **Yes** | `ADMIN` or `USER`. |
| `role` | String | No | The role this rule applies to. When absent, the rule applies to any role. |
| `user` | String | No | The specific user this rule applies to. When absent, the rule applies to any user. |
| `workspace` | String | No | The workspace scope for the permissions. When absent, the rule applies to any workspace. |
| `addressRange` | CIDR | No | IP address range restriction. |

### Access Levels

*   **ADMIN**: Grants administrative privileges within the scope.
    *   If `workspace` is absent: the rule grants administration on any workspace.
    *   If `workspace` is specific (e.g., `topp`): the rule grants Workspace Administrator rights for `topp`.
*   **USER**: Grants read-only user access to the workspace (can view but not modify).

## JSON Examples

### 1. Administrator for All Workspaces
Grants workspace administration rights on every workspace.

```json
{
  "priority": 0,
  "access": "ADMIN",
  "role": "ROLE_SYSADMIN"
}
```

### 2. Workspace Administrator
Allows a user to manage data, layers, and styles *only* within the `engineering` workspace.

```json
{
  "priority": 100,
  "access": "ADMIN",
  "user": "eng_lead",
  "workspace": "engineering"
}
```

### 3. Read-Only Auditor
Allows an auditor to view configurations for all workspaces but change nothing.

```json
{
  "priority": 500,
  "access": "USER",
  "role": "ROLE_AUDITOR"
}
```

## Management API

Admin Rules are managed via the REST API under `/api/adminrules`.

*   **GET /api/adminrules**: List all admin rules in priority order. Cursor-based pagination via the `limit` and `nextCursor` parameters.
*   **POST /api/adminrules**: Create a new admin rule. The optional `position` parameter controls how `priority` is interpreted.
*   **PATCH /api/adminrules/id/{id}**: Update an existing admin rule.
*   **DELETE /api/adminrules/id/{id}**: Delete an admin rule.
*   **DELETE /api/adminrules**: Delete all admin rules, returning the removed count.
*   **POST /api/adminrules/query**: Search admin rules with a filter.
*   **POST /api/adminrules/shift**, **POST /api/adminrules/id/{id}/swapwith/{id2}**: Re-prioritize admin rules.

## Differences from Data Rules

It is critical to distinguish between **Admin Rules** and **Data Rules**:

*   **Admin Rules** control the **configuration** (Can I add a layer? Can I change a style?).
*   **Data Rules** control the **content** (Can I see the map? Can I query the database?).

A user may have `ADMIN` rights to a workspace (via Admin Rule) but be denied viewing the actual map data if a specific `DENY` Data Rule exists for them.

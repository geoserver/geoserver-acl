# Workspace Administration

GeoServer ACL enables a delegated administration model, allowing designated users to manage specific workspaces without requiring full system administrator privileges. This is achieved through **Admin Rules**.

## Role Definition

A **Workspace Administrator** is a standard GeoServer user or role that has been granted administrative authority over one or more workspaces via an ACL Admin Rule.

Within their assigned workspaces, these administrators can:

*   **Manage Data**: Create and configure Data Stores (PostGIS, Shapefile, etc.).
*   **Publish Resources**: Configure and publish Layers and Layer Groups.
*   **Style Data**: Create and edit styles (SLD, CSS) limited to their workspace.
*   **View Settings**: Access workspace-specific configuration panels.

They **cannot**:

*   Modify global settings (Security, Services, Logging).
*   Access or administer workspaces they are not explicitly assigned to.
*   Manage global roles or users.

## Configuration

Workspace administration is configured in the **Admin Rules** section of the ACL management interface.

1.  **Grant Access**: An Admin Rule is created linking a `Role` (e.g., `ROLE_NYC_ADMIN`) to a `Workspace` (e.g., `nyc_data`) with `ADMIN` access.
2.  **Enforcement**: When a user with `ROLE_NYC_ADMIN` logs into the GeoServer Web UI, the interface automatically adjusts. The global sidebar is restricted, showing only the "Data" and "Layer Preview" sections relevant to the `nyc_data` workspace.

## Interface Behavior

### Web Administration

The GeoServer Web UI adapts to the user's permissions:

*   **Home Page**: Displays only authorized workspaces.
*   **Data Panel**: "Stores", "Layers", and "Styles" pages list only resources belonging to the managed workspaces.
*   **Service Configuration**: Workspace-specific service settings (WFS/WMS limits) are editable if the Admin Rule permits.

### REST API

Workspace administrators can manage resources via the standard GeoServer REST API. The ACL plugin intercepts these calls:

```http
GET /geoserver/rest/workspaces/nyc_data/datastores.json
```

*   **Authorized**: Returns the list of stores.
*   **Unauthorized** (e.g., accessing a different workspace): Returns `403 Forbidden`.

## Limitations & ACL Interaction

*   **Layer Security**: Being a workspace administrator does not automatically bypass *Data Access Rules*. If a specific layer has a strict "Deny" rule for the administrator's role, they may be able to see the layer configuration but not preview the data.
*   **Style Management**: Styles must be created inside the workspace. Global styles are read-only or invisible depending on configuration.
*   **Resource Creation**: Administrators can only create resources that reside strictly within their workspace.

## Troubleshooting

*   **"I can't see the 'Add Store' button"**: Verify that the Admin Rule explicitly grants `ADMIN` (not just `READ`) access to the workspace.
*   **"I can see the layer but not the data"**: Check **Data Access Rules**. Admin privileges control configuration access; Data Access rules control map/feature access. You may need a separate Data Rule granting `ACCESS` to the administrator role.
*   **Global vs. Workspace Styles**: Ensure styles are being created *within* the workspace. Attempts to edit global styles will fail.

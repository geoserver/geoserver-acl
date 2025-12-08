# Understanding Access Control

GeoServer ACL uses a priority-based rule system to determine exactly what resources a user can access. This page explains the core concepts behind these rules and how they are evaluated.

## Core Concepts

The authorization model is built around defining **Who** can do **What**, and **How** they can do it.

1.  **The Actor (Who)**: The user, role, or IP address initiating the request.
2.  **The Resource (What)**: The specific data being accessed (Workspace, Layer).
3.  **The Access Method (How)**: The service (e.g., WMS, WFS) and operation (e.g., GetMap) used to access the resource.

## Rule Structure

A rule is essentially a "match" condition followed by an "action".

### Match Conditions
A rule applies if the incoming request matches criteria defined by the administrator:

*   **Who**: Does the request come from a specific user (e.g., `jsmith`) or role (e.g., `ROLE_MANAGER`)?
*   **What**: Is the request for a specific workspace or layer?
*   **How**: Is this a WMS (map view) or WFS (data download) request?

### Actions
If a rule matches, one of the following actions is taken:

*   **ALLOW**: The request proceeds normally.
*   **DENY**: The request is blocked immediately.
*   **LIMIT**: The request proceeds, but with filters applied:
    *   **Spatial Filter**: Only features within a drawn polygon are returned.
    *   **Attribute Filter**: Sensitive columns (e.g., "owner_name", "phone_number") are removed from the result.

## How Rules Are Prioritized

Since multiple rules might match a single request, **Priority** is key.

1.  **Explicit Priority**: Administrators assign a numeric priority to each rule.
2.  **Evaluation Order**: The engine checks rules from highest priority to lowest.
3.  **First Match Wins**: The moment a rule matches the request, its action is applied, and further processing stops.
4.  **Default Fallback**: If *no* rules match, the default policy (usually **DENY**) is applied.

> **Tip:** Specific rules (e.g., "Allow `jsmith` to see `roads`") should always have higher priority than general rules (e.g., "Deny everyone access to `roads`").

## Practical Example

Imagine a city planning system with a **"Zoning"** layer containing sensitive property data.

| Priority | Actor | Service | Layer | Action | Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1 (High)** | `ROLE_ADMIN` | *Any* | Zoning | **ALLOW** | Admins see everything. |
| **2** | `ROLE_PLANNER` | *Any* | Zoning | **LIMIT** (City Center) | Planners only see zoning in the city center. |
| **3** | `ROLE_PUBLIC` | WMS | Zoning | **LIMIT** (Hide 'Owner') | Public can view the map, but cannot see the 'Owner' attribute. |
| **4 (Low)** | *Any* | WFS | Zoning | **DENY** | No one (except Admins/Planners above) can download the raw data. |

### In this scenario:

*   An **Admin** requesting the layer gets full access (Rule 1 matches).
*   A **Planner** gets spatially filtered data (Rule 2 matches).
*   A **Public** user sees the map but no owner names (Rule 3 matches).
*   A **Public** user trying to download the data (WFS) gets blocked (Rule 4 matches).

## Handling Access Errors

If a user reports they cannot see a layer or are missing data:

1.  **Check Permissions**: Ensure a rule exists that explicitly allows them access.
2.  **Check Priority**: Verify a higher-priority "DENY" rule isn't blocking them.
3.  **Check Filters**: If they see the layer but features are missing, a spatial filter might be active.

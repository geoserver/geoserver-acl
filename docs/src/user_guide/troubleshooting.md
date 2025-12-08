# Troubleshooting

This guide addresses common issues encountered when interacting with GeoServer ACL.

## Diagnosing Connectivity

Before investigating permission issues, verify that the ACL service is reachable and correctly integrated.

**Symptoms**: All requests fail, or the GeoServer logs show "Connection Refused" to the ACL service.

**Verification**:

1.  Check the ACL service health endpoint: `http://<acl-host>:8081/actuator/health`.
2.  Verify the GeoServer ACL plugin configuration in `security/acl/config.xml` points to the correct URL.

## Access Denied (HTTP 403)

**Symptom**: The server explicitly rejects the request with a `403 Forbidden` status.

**Causes & Solutions**:

*   **Missing Rule**: By default, ACL denies all access. Ensure a rule explicitly grants permission for the User/Role and Resource combination.
*   **Service Restriction**: A user may have `ACCESS` to a layer (WMS) but be blocked from downloading data (WFS). Check Service-level rules.
*   **Workspace Admin Limits**: A workspace administrator trying to access global resources or other workspaces will receive a 403.

**Diagnosis**:
Check the ACL audit logs or the HTTP response body. ACL often provides a specific exception code (e.g., `ServiceAccessDenied`).

## Empty or Incomplete Data

**Symptom**: The request succeeds (HTTP 200), but the map is blank, features are missing, or attributes are dropped.

**Causes & Solutions**:

*   **Spatial Filtering**: The user has `LIMIT` access restricted to a specific geometry. If the requested BBOX does not intersect this geometry, the result is valid but empty.
    *   *Test*: Try requesting a known valid area for that user.
*   **Attribute Filtering**: If specific columns are missing from WFS/GetFeatureInfo, an Attribute Filter is likely active.
    *   *Test*: Check if the user has `READ` access to the specific attributes in the Data Rules.
*   **Layer Group Visibility**: If a Layer Group is empty, the user might lack access to the individual layers contained within it.

## Authentication Failures (HTTP 401)

**Symptom**: Repeated login prompts or `401 Unauthorized`.

**Causes & Solutions**:

*   **Synchronization**: The user exists in GeoServer but not in the ACL database (or vice versa) if using synchronized user stores.
*   **Role Mapping**: The user is authenticated, but their roles do not match any active rules.
    *   *Diagnosis*: Check the `X-GeoServer-ACL-Roles` header in the debug logs to see what roles the ACL engine received.

## Diagnostic Tools

### cURL
Use cURL to isolate the API from client-side issues (like QGIS caching).

```bash
# Test Capabilities (Basic Auth)
curl -v -u user:password \
  "http://localhost:8080/geoserver/wfs?request=GetCapabilities"

# Test Specific Feature Access
curl -v -u user:password \
  "http://localhost:8080/geoserver/wfs?request=GetFeature&typeName=ws:layer&count=1"
```

### Browser DevTools
1.  Open Network Tab.
2.  Trigger the map load.
3.  Inspect red (failed) requests.
    *   **404**: Resource doesn't exist (or is hidden by ACL).
    *   **403**: Permission denied.
    *   **500**: System error (check server logs).

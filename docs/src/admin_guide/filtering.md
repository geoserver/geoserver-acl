# Advanced Filtering

This guide details the configuration for Spatial and Attribute filtering within Data Access Rules.

## Spatial Filtering

Spatial filtering restricts access to a defined geometric area. It is configured via the `ruleLimits` object in a rule definition.

### Configuration Schema

| Field | Type | Description |
| :--- | :--- | :--- |
| `allowedArea` | WKT String | The geometry (Polygon/MultiPolygon) defining the permitted area. |
| `spatialFilterType` | Enum | The strategy for handling features crossing the boundary: `INTERSECT` or `CLIP`. |

### Filter Types

*   **INTERSECT** (Default): Returns complete features if they spatially interact with the `allowedArea`.
    *   *Performance*: Fast. Uses database spatial indexes.
    *   *Use Case*: Standard row-level security.
*   **CLIP**: Geometrically crops features to the `allowedArea`.
    *   *Performance*: Slower. Requires CPU-intensive geometry intersection calculations per feature.
    *   *Use Case*: Strict security where users must not see geometry outside the boundary.

### JSON Example

```json
{
  "access": "LIMIT",
  "ruleLimits": {
    "allowedArea": "POLYGON((-105 40, -104 40, -104 41, -105 41, -105 40))",
    "spatialFilterType": "CLIP"
  }
}
```

## Attribute Filtering

Attribute filtering hides specific columns from the output. It is configured via the `layerDetails` object.

### Configuration Schema

| Field | Type | Description |
| :--- | :--- | :--- |
| `attributes` | Object | Container for attribute rules. |
| `accessType` | Enum | `READONLY` or `READWRITE`. |
| `includedAttributes` | Array | Allow-list of visible attributes. |
| `excludedAttributes` | Array | Block-list of hidden attributes. |

**Note**: Use either `includedAttributes` OR `excludedAttributes`, not both.

### JSON Example (Exclude Mode)

```json
{
  "access": "LIMIT",
  "layerDetails": {
    "attributes": {
      "accessType": "READONLY",
      "excludedAttributes": ["social_security_number", "salary"]
    }
  }
}
```

## Additional Constraints

### Coordinate Reference System (CRS)
Restricts the output CRS users can request (e.g., to prevent reprojection load).

```json
{
  "access": "LIMIT",
  "ruleLimits": {
    "allowedCRS": ["EPSG:4326", "EPSG:3857"]
  }
}
```

### Catalog Mode
Controls how restricted layers appear in the capabilities document.

| Mode | Description |
| :--- | :--- |
| `HIDE` | Layer is removed from Capabilities if user lacks access. (Secure) |
| `CHALLENGE` | Layer is visible; accessing it triggers a 401 Challenge. |
| `MIXED` | Layer is visible; data is filtered transparently. |

```json
{
  "access": "LIMIT",
  "layerDetails": {
    "catalogMode": "HIDE"
  }
}
```

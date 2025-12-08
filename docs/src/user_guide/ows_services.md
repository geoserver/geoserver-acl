# OGC Services Integration

GeoServer ACL enforces authorization controls across standard OGC Web Services (OWS). This page details how these controls manifest in service responses and workflows.

## Common Authorization Patterns

Across all services (WMS, WFS, WCS, etc.), ACL applies consistent enforcement mechanisms:

### Capabilities Filtering
Service metadata documents (e.g., `GetCapabilities`) are dynamically filtered. Users only see layers, feature types, or coverages they are explicitly authorized to access. If a user has no access to a resource, it is completely omitted from the capabilities response.

> A future enhancement could involve overriding the layers bounding boxes in the capabilities responses to match the spatial constraints.

### Spatial Constraints
Requests involving spatial queries (e.g., `bbox` parameters) are intersected with the user's authorized area.

*   **Intersection**: Only data within the user's allowed geometry is returned.

> A nice future enhancement may involve adding a **Disjoint** constraint: Requests strictly outside the authorized area may return empty results or specific exceptions depending on the service.

### Attribute Filtering
When attribute-level rules are active, the service response schema is modified on the fly. Restricted columns are removed from:

*   WFS `GetFeature` responses (GML/JSON).
*   WMS `GetFeatureInfo` results.

> A future enhancement could be limiting WCS coverage bands

---

## Service-Specific Behaviors

### Web Map Service (WMS)
*   **GetMap**: The generated map image is clipped to the authorized geometry. Areas outside the user's permissions appear transparent or empty.
*   **GetFeatureInfo**: Queries are validated against spatial permissions. Clicking on a visible feature outside the user's interactive allowance returns no data.

### Web Feature Service (WFS)
*   **GetFeature**: Returns only features satisfying the spatial filter.
*   **Transactional WFS (WFS-T)**: Write operations (`Insert`, `Update`, `Delete`) are strictly validated. Users cannot modify features outside their spatial scope or edit read-only attributes.
    *   Attempts to write to restricted areas result in a Service Exception.

### Web Coverage Service (WCS)
*   **GetCoverage**: Raster data is cropped to the authorized region. Requesting a subset entirely outside the allowed area results in an exception or empty coverage.

### Web Map Tile Service (WMTS)
*   **GetTile**: Access is validated per tile.
    *   Tiles intersecting the authorized area are returned.
    *   Tiles fully outside the authorized area return a 403 Forbidden or a blank tile, depending on configuration.
    *   **Caching Implications**: GeoWebCache integration ensures that cached tiles respect underlying ACL rules, though this may impact cache hit rates if user-specific rules are highly fragmented.

### Web Processing Service (WPS)
*   **Execute**: Processes are authorized based on the input data. If a process attempts to operate on a restricted layer or region, execution is blocked or the input is pre-filtered.

<!-- revisit: test CSW integration
### Catalog Service for the Web (CSW)
*   **GetRecords**: Metadata records are filtered. Users cannot discover metadata for datasets they are not permitted to view.
-->

---

## Error Handling

Clients interacting with ACL-protected services should be prepared for standard OGC exception reports and HTTP status codes:

*   **HTTP 403 Forbidden**: The user lacks permission for the requested operation or resource.
*   **Empty Result Sets**: A valid request that intersects no authorized data (e.g., a WFS query for a region the user cannot see) returns an empty feature collection, not an error.
*   **Service Exceptions**: XML/JSON exception reports detailing specific constraint violations (e.g., "Write access denied for attribute 'zoning_code'").

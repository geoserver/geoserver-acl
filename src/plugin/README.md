# GeoServer ACL (Access Control List) plugin





## Differences with GeoFence 3.6.0

### Default authorization execution

GeoFence runs the authorization in-place, the ACL plugin uses the ACL OpenAPI REST interface to fetch
the authorization `AccessInfo` from the remote server.

### Cache

GeoFence uses a local cache for rules, admin rules, and `AccessInfo` grants, which gets
stale whenever a rule is configured, and needs manual intervention to invalidate the cache,
either through the WEB UI or the REST API.

The ACL plugin relies on the server, and it's up to the server to use a cache or not.
That said, Authorization service client adaptor may use a short-lived cache where
entries expire for example in one second, to avoid overwhelming the server under
high concurrency.

### Role names

Role names don't need to exist in any of the GeoServer configured Role Services

### Role filtering

Role names are used by default to request access grants, contrary to GeoFence

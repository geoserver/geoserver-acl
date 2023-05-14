# GeoServer Access Control List (ACL)

GeoServer ACL is an advanced authorization system for [GeoServer](https://geoserver.org/).

It consists of an independent application service that manages access rules,
and a GeoServer plugin that requests authorization limits on a per-request basis.

As an administrator you'll use GeoServer ACL to define rules
that grant or deny access to published resources based on
service request properties such user credentials, the type
of OWS service, and layers being requested.

These rules can be as open as to grant or deny access
to whole GeoServer workspaces, or as granular as to specify
which geographical areas and layer attributes to allow a
specific user or user group to see.

As a user you'll perform requests to GeoServer such as WMS GetMap or WFS GetFeatures,
and the ACL-based authorization engine will limit the visibility
of the resources and contents of the responses to those matching
the rules that apply to the request properties and the authenticated
user credentials.

GeoServer ACL is not an authentication provider. It's an authorization
manager that will use the authenticated user credentials, whether
they come from Basic HTTP, OAuth2/OpenID Connect, or whatever authentication
mechanism GeoServer is using, to resolve the access rules that apply
to each particular request.

GeoServer ACL is Open Source, born as a
[fork](https://en.wikipedia.org/wiki/Fork_%28software_development%29) of 
[GeoFence](https://github.com/geoserver/geofence).
As such, it follows the same logic to define data access and administrative
access rules. So if you're familiar with GeoFence, it'll be easy to reason
about GeoServer ACL.



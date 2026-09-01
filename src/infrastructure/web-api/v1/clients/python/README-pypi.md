# GeoServer ACL Python Client

Python client for [GeoServer ACL](https://geoserver.org/geoserver-acl/), the
advanced authorization service for GeoServer: manage data access rules and
workspace admin rules, and query authorization decisions.

The client is generated from the service's OpenAPI specification
(synchronous, urllib3-based). The client version matches the server release
it was built with; install the version matching your server.

## Installation

```bash
pip install geoserver-acl-client==@VERSION@
```

## Usage

```python
from geoserver_acl_client import (
    AccessRequest,
    ApiClient,
    AuthorizationApi,
    Configuration,
    DataRulesApi,
    GrantType,
    Rule,
)

configuration = Configuration(
    host="http://localhost:8080/acl/api", username="admin", password="s3cr3t"
)

with ApiClient(configuration) as client:
    # Rules match on the fields you set; a field left absent matches anything.
    # Lower priority values are evaluated first, and the first match decides.
    rules = DataRulesApi(client)
    rules.create_rule(
        Rule(priority=10, access=GrantType.ALLOW, role="ROLE_USER", workspace="users_ws")
    )

    # The same absence semantics apply to authorization requests.
    authorization = AuthorizationApi(client)
    request = AccessRequest(
        user="john",
        roles=["ROLE_USER"],
        workspace="users_ws",
        layer="a_layer",
        service="WMS",
        request="GetMap",
    )
    access = authorization.get_access_info(request)
    print(access.grant)  # GrantType.ALLOW
```

## Try it locally

Run the service with the `dev` profile (in-memory database, credentials
`admin`/`s3cr3t`):

```bash
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev geoservercloud/geoserver-acl:@VERSION@
```

The interactive API documentation (Swagger UI) is then served at
`http://localhost:8080/acl/openapi/swagger-ui/index.html`.

A complete runnable example, starting the service through testcontainers,
lives in the repository:
[examples/python-client](https://github.com/geoserver/geoserver-acl/tree/main/examples/python-client).

## Links

- [Documentation](https://geoserver.org/geoserver-acl/)
- [API documentation](https://geoserver.org/geoserver-acl/api/)
- [Source code and issue tracker](https://github.com/geoserver/geoserver-acl)
- [OpenAPI specification](https://github.com/geoserver/geoserver-acl/blob/main/src/infrastructure/web-api/spec/src/main/resources/geoserver-acl-web-api-spec-v1.yaml)

## License

GPL-2.0. (c) Open Source Geospatial Foundation.

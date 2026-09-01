# Python Client Example

Demonstrates the [geoserver-acl-client](https://pypi.org/project/geoserver-acl-client/)
package against a real GeoServer ACL service: creating data access rules and
querying authorization decisions. The service runs from the
`geoservercloud/geoserver-acl` docker image with the `dev` profile
(in-memory database), started through
[testcontainers](https://testcontainers-python.readthedocs.io/).

See [test_authorization_example.py](test_authorization_example.py) for the
commented walkthrough.

## Running from this repository

Builds the wheel and the docker image from the working tree, then runs the
example against them:

```bash
make package build-image dist-python-client
make test-python-example
```

## Running standalone

Uses the published client and any published image tag:

```bash
pip install -r requirements.txt
ACL_IMAGE=geoservercloud/geoserver-acl:3.0.3 python -m pytest -v
```

Docker must be running in both cases.

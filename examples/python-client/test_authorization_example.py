# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.

"""Demonstrates how to use the geoserver-acl-client package to manage
data access rules and perform authorization requests against a real
GeoServer ACL service.

The service runs from the geoservercloud/geoserver-acl docker image with
the `dev` profile (in-memory database, default credentials), started here
through testcontainers. Every rule created below lives only for the
lifetime of that container.

Run it from the repository with `make test-python-example`, or standalone
against any published image:

    pip install -r requirements.txt
    ACL_IMAGE=geoservercloud/geoserver-acl:3.0.3 python -m pytest -v
"""

import os
import re

import pytest
from testcontainers.core.container import DockerContainer
from testcontainers.core.wait_strategies import LogMessageWaitStrategy

from geoserver_acl_client import (
    AccessRequest,
    ApiClient,
    AuthorizationApi,
    Configuration,
    DataRulesApi,
    GrantType,
    Rule,
)

IMAGE = os.environ.get("ACL_IMAGE", "geoservercloud/geoserver-acl:3.0-SNAPSHOT")


@pytest.fixture(scope="module")
def acl_api_url():
    """Starts the ACL service in a container and yields its API base URL.

    The dev profile keeps everything in memory: no database to provision,
    and the default admin credentials are admin/s3cr3t.
    """
    container = (
        DockerContainer(IMAGE)
        .with_env("SPRING_PROFILES_ACTIVE", "dev")
        .with_exposed_ports(8080)
        .waiting_for(LogMessageWaitStrategy(re.compile(r"Started AccesControlListApplication")))
    )
    container.start()
    try:
        host = container.get_container_host_ip()
        port = container.get_exposed_port(8080)
        yield f"http://{host}:{port}/acl/api"
    finally:
        container.stop()


@pytest.fixture(scope="module")
def api_client(acl_api_url):
    configuration = Configuration(host=acl_api_url, username="admin", password="s3cr3t")
    with ApiClient(configuration) as client:
        yield client


def test_rules_and_authorization(api_client):
    rules = DataRulesApi(api_client)
    authorization = AuthorizationApi(api_client)

    # A rule matches a request on the fields you set; a field left absent
    # matches anything. Rules are evaluated in priority order (lower values
    # first) and the first match decides.
    rules.create_rule(Rule(priority=10, access=GrantType.ALLOW, role="ROLE_USER", workspace="users_ws"))
    rules.create_rule(Rule(priority=20, access=GrantType.ALLOW, role="ROLE_EDITOR", workspace="editors_ws"))

    # john has ROLE_USER: the first rule grants access to users_ws. Like in
    # rules, an absent request field means "no constraint" - never send '*'.
    request = AccessRequest(
        user="john",
        roles=["ROLE_AUTHENTICATED", "ROLE_USER"],
        workspace="users_ws",
        layer="a_layer",
        service="WMS",
        request="GetMap",
    )
    access = authorization.get_access_info(request)
    assert access.grant == GrantType.ALLOW

    # The decision reports which rules produced it, useful for auditing.
    assert access.matching_rules

    # No rule grants john anything on editors_ws, and when nothing matches
    # the decision is DENY (and no matching rules are reported).
    request = AccessRequest(
        user="john",
        roles=["ROLE_AUTHENTICATED", "ROLE_USER"],
        workspace="editors_ws",
        layer="a_layer",
        service="WMS",
        request="GetMap",
    )
    access = authorization.get_access_info(request)
    assert access.grant == GrantType.DENY

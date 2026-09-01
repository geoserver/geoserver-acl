# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.

import os

import pytest
from geoserver_acl_client import (
    ApiClient,
    AuthorizationApi,
    Configuration,
    DataRulesApi,
    WorkspaceAdminRulesApi,
)


@pytest.fixture(scope="session")
def api_client():
    configuration = Configuration(
        host=os.environ.get("ACL_URL", "http://localhost:8080/acl/api"),
        username=os.environ.get("ACL_USERNAME", "admin"),
        password=os.environ.get("ACL_PASSWORD", "s3cr3t"),
    )
    with ApiClient(configuration) as client:
        yield client


@pytest.fixture
def data_rules(api_client):
    api = DataRulesApi(api_client)
    yield api
    api.delete_all_rules()


@pytest.fixture
def admin_rules(api_client):
    api = WorkspaceAdminRulesApi(api_client)
    yield api
    api.delete_all_admin_rules()


@pytest.fixture
def authorization(api_client):
    return AuthorizationApi(api_client)

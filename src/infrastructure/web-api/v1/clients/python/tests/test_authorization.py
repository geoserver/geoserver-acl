# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.

from geoserver_acl_client import (
    AccessRequest,
    AdminAccessRequest,
    AdminGrantType,
    AdminRule,
    GrantType,
    Rule,
)


def test_matching_allow_rule_grants_access(data_rules, authorization):
    data_rules.create_rule(
        Rule(priority=1, access=GrantType.ALLOW, workspace="topp", layer="states")
    )

    request = AccessRequest(
        user="someuser",
        roles=["ROLE_USER"],
        service="WMS",
        request="GetMap",
        workspace="topp",
        layer="states",
    )
    access = authorization.get_access_info(request)

    assert access.grant == GrantType.ALLOW


def test_default_decision_is_deny(data_rules, authorization):
    request = AccessRequest(
        user="someuser",
        roles=["ROLE_USER"],
        workspace="nosuchworkspace",
        layer="nosuchlayer",
    )
    access = authorization.get_access_info(request)

    assert access.grant == GrantType.DENY


def test_admin_rule_controls_workspace_admin_access(admin_rules, authorization):
    admin_rules.create_admin_rule(
        AdminRule(priority=1, access=AdminGrantType.ADMIN, workspace="topp", role="ROLE_GIS")
    )

    granted = authorization.get_admin_authorization(
        AdminAccessRequest(user="bob", roles=["ROLE_GIS"], workspace="topp")
    )
    denied = authorization.get_admin_authorization(
        AdminAccessRequest(user="alice", roles=["ROLE_OTHER"], workspace="topp")
    )

    assert granted.admin is True
    assert denied.admin is False

# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.

import pytest
from geoserver_acl_client import GrantType, Rule
from geoserver_acl_client.exceptions import NotFoundException


def test_create_and_get_rule(data_rules):
    rule = Rule(priority=1, access=GrantType.ALLOW, workspace="topp", layer="states")

    created = data_rules.create_rule(rule)

    assert created.id is not None
    fetched = data_rules.get_rule_by_id(created.id)
    assert fetched.workspace == "topp"
    assert fetched.layer == "states"
    assert fetched.access == GrantType.ALLOW


def test_update_rule(data_rules):
    created = data_rules.create_rule(Rule(priority=1, access=GrantType.DENY, workspace="tiger"))

    created.layer = "roads"
    updated = data_rules.update_rule_by_id(created.id, created)

    assert updated.layer == "roads"
    assert data_rules.get_rule_by_id(created.id).layer == "roads"


def test_delete_rule(data_rules):
    created = data_rules.create_rule(Rule(priority=1, access=GrantType.ALLOW, workspace="scratch"))

    data_rules.delete_rule_by_id(created.id)

    assert data_rules.rule_exists_by_id(created.id) is False
    with pytest.raises(NotFoundException):
        data_rules.get_rule_by_id(created.id)


def test_list_rules_in_priority_order(data_rules):
    second = data_rules.create_rule(Rule(priority=20, access=GrantType.DENY, workspace="ws2"))
    first = data_rules.create_rule(Rule(priority=10, access=GrantType.ALLOW, workspace="ws1"))

    rules = data_rules.get_rules()

    assert [r.id for r in rules] == [first.id, second.id]


def test_count_rules(data_rules):
    assert data_rules.count_all_rules() == 0

    data_rules.create_rule(Rule(priority=1, access=GrantType.ALLOW, workspace="a"))
    data_rules.create_rule(Rule(priority=2, access=GrantType.DENY, workspace="b"))

    assert data_rules.count_all_rules() == 2

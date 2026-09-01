# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.

from geoserver_acl_client import AdminGrantType, AdminRule


def test_create_and_get_admin_rule(admin_rules):
    rule = AdminRule(priority=1, access=AdminGrantType.ADMIN, workspace="topp", role="ROLE_GIS")

    created = admin_rules.create_admin_rule(rule)

    assert created.id is not None
    fetched = admin_rules.get_admin_rule_by_id(created.id)
    assert fetched.workspace == "topp"
    assert fetched.role == "ROLE_GIS"
    assert fetched.access == AdminGrantType.ADMIN


def test_update_admin_rule(admin_rules):
    created = admin_rules.create_admin_rule(
        AdminRule(priority=1, access=AdminGrantType.USER, workspace="tiger")
    )

    created.access = AdminGrantType.ADMIN
    updated = admin_rules.update_admin_rule(created.id, created)

    assert updated.access == AdminGrantType.ADMIN
    assert admin_rules.get_admin_rule_by_id(created.id).access == AdminGrantType.ADMIN


def test_delete_admin_rule(admin_rules):
    created = admin_rules.create_admin_rule(
        AdminRule(priority=1, access=AdminGrantType.ADMIN, workspace="scratch")
    )

    admin_rules.delete_admin_rule_by_id(created.id)

    assert admin_rules.admin_rule_exists_by_id(created.id) is False


def test_list_admin_rules_in_priority_order(admin_rules):
    second = admin_rules.create_admin_rule(
        AdminRule(priority=20, access=AdminGrantType.USER, workspace="b")
    )
    first = admin_rules.create_admin_rule(
        AdminRule(priority=10, access=AdminGrantType.ADMIN, workspace="a")
    )

    rules = admin_rules.find_all_admin_rules()

    assert [r.id for r in rules] == [first.id, second.id]

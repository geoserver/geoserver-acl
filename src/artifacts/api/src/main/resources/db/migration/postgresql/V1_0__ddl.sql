create sequence acl_adminrule_sequence start 1 increment 1;
create sequence acl_rule_sequence start 1 increment 1;

    create table acl_adminrule (
       id int8 not null,
        createdBy TEXT,
        createdDate timestamp not null,
        lastModifiedBy TEXT,
        lastModifiedDate timestamp,
        grant_type TEXT not null,
        description TEXT,
        extId TEXT,
        ip_high int8,
        ip_low int8,
        ip_size int4,
        instance TEXT not null,
        rolename TEXT not null,
        username TEXT not null,
        workspace TEXT not null,
        name TEXT,
        priority int8 not null,
        primary key (id)
    );

    create table acl_layer_attributes (
       details_id int8 not null,
        access_type TEXT,
        data_type TEXT,
        name TEXT not null
    );

    create table acl_layer_styles (
       details_id int8 not null,
        ld_styleName TEXT
    );

    create table acl_rule (
       id int8 not null,
        createdBy TEXT,
        createdDate timestamp not null,
        lastModifiedBy TEXT,
        lastModifiedDate timestamp,
        description TEXT,
        extId TEXT,
        grant_type TEXT not null,
        ip_high int8,
        ip_low int8,
        ip_size int4,
        instance TEXT not null,
        layer TEXT not null,
        request TEXT not null,
        rolename TEXT not null,
        service TEXT not null,
        subfield TEXT not null,
        username TEXT not null,
        workspace TEXT not null,
        ld_area GEOMETRY,
        ld_catalog_mode TEXT,
        ld_cql_filter_read TEXT,
        ld_cql_filter_write TEXT,
        ld_default_style TEXT,
        ld_spatial_filter_type TEXT,
        ld_type TEXT,
        name TEXT,
        priority int8 not null,
        limits_area GEOMETRY,
        limits_catalog_mode TEXT,
        limits_spatial_filter_type TEXT,
        primary key (id)
    );
create index idx_adminrule_priority on acl_adminrule (priority);
create index idx_adminrule_username on acl_adminrule (username);
create index idx_adminrule_rolename on acl_adminrule (rolename);
create index idx_adminrule_workspace on acl_adminrule (workspace);
create index idx_adminrule_grant_type on acl_adminrule (grant_type);

    alter table if exists acl_adminrule 
       add constraint UKr5m8k1a19ac1scuv9ydlxr78l unique (instance, username, rolename, workspace, ip_low, ip_high, ip_size);

    alter table if exists acl_adminrule 
       add constraint UK_98xhi22anfnb7xafme06ba58r unique (extId);

    alter table if exists acl_layer_attributes 
       add constraint acl_layer_attributes_name unique (details_id, name);
create index idx_rule_priority on acl_rule (priority);
create index idx_rule_service on acl_rule (service);
create index idx_rule_request on acl_rule (request);
create index idx_rule_workspace on acl_rule (workspace);
create index idx_rule_layer on acl_rule (layer);

    alter table if exists acl_rule 
       add constraint UK_d0ydh6l1d3u2tj2h22cc8sqt3 unique (extId);

    alter table if exists acl_layer_attributes 
       add constraint fk_attribute_layer 
       foreign key (details_id) 
       references acl_rule;

    alter table if exists acl_layer_styles 
       add constraint fk_styles_layer 
       foreign key (details_id) 
       references acl_rule;

ALTER TABLE acl_adminrule 
	DROP CONSTRAINT acl_adminrule_id_uk;
	
ALTER TABLE acl_adminrule DROP COLUMN instance;

ALTER TABLE acl_adminrule 
    ADD CONSTRAINT acl_adminrule_id_uk UNIQUE (username, rolename, workspace, ip_low, ip_high, ip_size);

ALTER TABLE acl_rule DROP COLUMN instance;

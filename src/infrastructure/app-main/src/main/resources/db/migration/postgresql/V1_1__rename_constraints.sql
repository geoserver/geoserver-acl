ALTER TABLE acl_adminrule	
	RENAME CONSTRAINT UKr5m8k1a19ac1scuv9ydlxr78l 
	TO acl_adminrule_id_uk; 

ALTER TABLE acl_adminrule 
	RENAME CONSTRAINT UK_98xhi22anfnb7xafme06ba58r
	TO acl_adminrule_extid_uk;


ALTER TABLE acl_rule 
	RENAME CONSTRAINT UK_d0ydh6l1d3u2tj2h22cc8sqt3
	TO acl_rule_extid_uk;

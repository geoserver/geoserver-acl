package org.geoserver.cloud.acl.autoconfigure.api;

import org.geoserver.acl.api.server.config.AuthorizationApiConfiguration;
import org.geoserver.acl.api.server.config.RulesApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({RulesApiConfiguration.class, AuthorizationApiConfiguration.class})
public class RulesApiAutoConfiguration {}

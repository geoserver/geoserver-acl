package org.geoserver.acl.plugin.autoconfigure.accessmanager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
        prefix = "geoserver.acl",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public @interface ConditionalOnAclEnabled {}

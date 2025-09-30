/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.adminrules.AdminRuleFilter;
import org.geoserver.acl.domain.filter.Filter;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.filter.predicate.FilterType;
import org.geoserver.acl.domain.filter.predicate.InSetPredicate;
import org.geoserver.acl.domain.filter.predicate.TextFilter;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.jpa.model.QAdminRule;
import org.geoserver.acl.jpa.model.QAdminRuleIdentifier;
import org.geoserver.acl.jpa.model.QRule;
import org.geoserver.acl.jpa.model.QRuleIdentifier;

@Slf4j
class PredicateMapper {

    public Predicate toPredicate(RuleQuery<? extends Filter<?>> query) {
        return query.getFilter().flatMap(this::mapPredicate).orElseGet(BooleanBuilder::new);
    }

    Optional<BooleanExpression> toPriorityPredicate(OptionalLong pstart) {
        if (pstart.isPresent()) {
            return Optional.of(QRule.rule.priority.goe(pstart.getAsLong()));
        }
        return Optional.empty();
    }

    public Optional<Predicate> toPredicate(AdminRuleFilter filter) {
        if (AdminRuleFilter.any().equals(filter)) {
            return Optional.empty();
        }

        QAdminRuleIdentifier qIdentifier = QAdminRule.adminRule.identifier;

        Predicate grantType = map(filter.getGrantType(), QAdminRule.adminRule.access);
        Predicate user = map(filter.getUser(), qIdentifier.username);
        Predicate role = map(filter.getRole(), qIdentifier.rolename);
        // Predicate address = map(filter.getSourceAddress(), identifier.addressRange);
        Predicate ws = map(filter.getWorkspace(), qIdentifier.workspace);
        BooleanBuilder predicate = new BooleanBuilder()
                .and(grantType)
                .and(user)
                .and(role)
                // .and(address)
                .and(ws);

        log.debug("Filter    : {}", filter);
        log.debug("Predicate : {}", predicate);
        return Optional.ofNullable(predicate);
    }

    private Predicate map(AdminGrantType grantType, EnumPath<org.geoserver.acl.jpa.model.AdminGrantType> access) {

        if (null == grantType) return null;
        switch (grantType) {
            case ADMIN:
                return access.eq(org.geoserver.acl.jpa.model.AdminGrantType.ADMIN);
            case USER:
                return access.eq(org.geoserver.acl.jpa.model.AdminGrantType.USER);
            default:
                throw new IllegalArgumentException("Unknown AdminGrantType: " + grantType);
        }
    }

    Optional<Predicate> mapPredicate(Filter<?> filter) {
        if (filter instanceof RuleFilter ruleFilter) return toPredicate(ruleFilter);
        if (filter instanceof AdminRuleFilter ruleFilter) return toPredicate(ruleFilter);
        return Optional.empty();
    }

    public Optional<Predicate> toPredicate(RuleFilter filter) {
        if (RuleFilter.any().equals(filter)) {
            return Optional.empty();
        }

        QRuleIdentifier qIdentifier = QRule.rule.identifier;

        Predicate user = map(filter.getUser(), qIdentifier.username);
        Predicate role = map(filter.getRole(), qIdentifier.rolename);

        Predicate service = map(filter.getService(), qIdentifier.service);
        Predicate request = map(filter.getRequest(), qIdentifier.request);
        Predicate subfield = map(filter.getSubfield(), qIdentifier.subfield);

        // Predicate address = map(filter.getSourceAddress(), identifier.addressRange);

        Predicate ws = map(filter.getWorkspace(), qIdentifier.workspace);
        Predicate layer = map(filter.getLayer(), qIdentifier.layer);

        BooleanBuilder predicate = new BooleanBuilder()
                .and(user)
                .and(role)
                .and(service)
                .and(request)
                .and(subfield)
                // .and(address)
                .and(ws)
                .and(layer);

        log.trace("Filter    : {}", filter);
        log.trace("Predicate : {}", predicate);
        return Optional.ofNullable(predicate);
    }

    Predicate map(TextFilter filter, StringPath propertyPath) {
        if (null == filter) return null;

        final FilterType type = filter.getType();

        final boolean includeDefault = filter.isIncludeDefault();

        switch (type) {
            case ANY:
                return null;
            case DEFAULT:
                return propertyPath.eq("*");
            case NAMEVALUE: {
                final String text = filter.getText();
                if (text == null) throw new IllegalArgumentException("Can't map TextFilter with empty value " + text);

                if (includeDefault) {
                    return propertyPath.in("*", text);
                }
                return propertyPath.eq(text);
            }
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Unknown or unexpected FilterType for TextFilter: " + type);
        }
    }

    Predicate map(InSetPredicate<String> filter, StringPath propertyPath) {
        if (null == filter) return null;

        final FilterType type = filter.getType();

        final boolean includeDefault = filter.isIncludeDefault();

        switch (type) {
            case ANY:
                return null;
            case DEFAULT:
                return propertyPath.eq("*");
            case NAMEVALUE: {
                final Set<String> values = filter.getValues();
                if (values == null || values.isEmpty())
                    throw new IllegalArgumentException("Can't map TextFilter with empty value " + values);

                if (includeDefault) {
                    return propertyPath.in(
                            Stream.concat(Stream.of("*"), values.stream()).toList());
                }
                return propertyPath.in(values);
            }
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Unknown or unexpected FilterType for TextFilter: " + type);
        }
    }
}

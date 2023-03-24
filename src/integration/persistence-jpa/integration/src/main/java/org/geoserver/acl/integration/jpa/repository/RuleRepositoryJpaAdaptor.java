/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.repository;

import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.LIMIT;
import static org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper.decodeId;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.domain.rules.InsertPosition;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper;
import org.geoserver.acl.jpa.model.GrantType;
import org.geoserver.acl.jpa.model.LayerDetails;
import org.geoserver.acl.jpa.model.QRule;
import org.geoserver.acl.jpa.model.RuleIdentifier;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.jpa.repository.TransactionReadOnly;
import org.geoserver.acl.jpa.repository.TransactionRequired;
import org.geoserver.acl.jpa.repository.TransactionSupported;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

@Slf4j
@TransactionSupported
public class RuleRepositoryJpaAdaptor implements RuleRepository {

    private final EntityManager em;

    private final JpaRuleRepository jparepo;
    private final RuleJpaMapper modelMapper;
    private final PredicateMapper queryMapper;

    @Setter
    private @NonNull Consumer<RuleEvent> eventPublisher =
            r -> {
                // no-op
            };

    public RuleRepositoryJpaAdaptor(
            EntityManager em, JpaRuleRepository jparepo, RuleJpaMapper mapper) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(jparepo);
        Objects.requireNonNull(mapper);
        this.em = em;
        this.modelMapper = mapper;
        this.jparepo = jparepo;
        this.queryMapper = new PredicateMapper();
    }

    private PriorityResolver<org.geoserver.acl.jpa.model.Rule> priorityResolver() {
        return new PriorityResolver<>(jparepo, org.geoserver.acl.jpa.model.Rule::getPriority);
    }

    @Override
    public Optional<Rule> findById(@NonNull String id) {
        return jparepo.findById(decodeId(id)).map(modelMapper::toModel);
    }

    @Override
    public Optional<Rule> findOneByPriority(long priority) {
        try {
            return jparepo.findOne(QRule.rule.priority.eq(priority)).map(modelMapper::toModel);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new IllegalStateException("There are multiple Rules with priority " + priority);
        }
    }

    @Override
    public int count() {
        return (int) jparepo.count();
    }

    @Override
    public int count(RuleFilter filter) {
        Optional<Predicate> predicate = queryMapper.toPredicate(filter);
        Long count = predicate.map(jparepo::count).orElseGet(jparepo::count);
        return count.intValue();
    }

    @Override
    public Stream<Rule> findAll() {
        return findAll(RuleQuery.of());
    }

    @Override
    @TransactionReadOnly
    public Stream<Rule> findAll(@NonNull RuleQuery<RuleFilter> query) {

        Predicate predicate = queryMapper.toPredicate(query);
        final java.util.function.Predicate<? super Rule> postFilter =
                filterByAddress(query.getFilter());

        if (query.getNextId() != null) {
            Long nextId = decodeId(query.getNextId());
            predicate = QRule.rule.id.goe(nextId).and(predicate);
        }

        CloseableIterator<org.geoserver.acl.jpa.model.Rule> iterator = query(predicate);

        try (Stream<org.geoserver.acl.jpa.model.Rule> stream = stream(iterator)) {
            Stream<Rule> rules = stream.map(modelMapper::toModel).filter(postFilter);
            final Integer pageSize = query.getLimit();
            if (null != pageSize) {
                rules = rules.limit(query.getLimit());
            }
            return rules.collect(Collectors.toList()).stream();
        }
    }

    private CloseableIterator<org.geoserver.acl.jpa.model.Rule> query(Predicate predicate) {

        CloseableIterator<org.geoserver.acl.jpa.model.Rule> iterator =
                new JPAQuery<org.geoserver.acl.jpa.model.Rule>(em)
                        .from(QRule.rule)
                        .where(predicate)
                        .orderBy(new OrderSpecifier<>(Order.ASC, QRule.rule.priority))
                        .iterate();
        return iterator;
    }

    private Stream<org.geoserver.acl.jpa.model.Rule> stream(
            CloseableIterator<org.geoserver.acl.jpa.model.Rule> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .onClose(iterator::close);
    }

    private java.util.function.Predicate<? super Rule> filterByAddress(
            Optional<RuleFilter> filter) {

        if (filter.isEmpty()) return r -> true;
        IPAddressRangeFilter ipFilter = filter.get().getSourceAddress();
        return rule -> {
            return ipFilter.test(rule.getIdentifier().getAddressRange());
        };
    }

    @Override
    @TransactionRequired
    public Rule save(Rule rule) {
        Objects.requireNonNull(rule.getId());
        findDup(rule).ifPresent(this::throwConflict);

        org.geoserver.acl.jpa.model.Rule entity = getOrThrowIAE(rule.getId());
        removeLayerDetailsIfNotApplicableAnyMore(rule, entity);

        PriorityResolver<org.geoserver.acl.jpa.model.Rule> priorityResolver = priorityResolver();
        long finalPriority =
                priorityResolver.resolvePriorityUpdate(entity.getPriority(), rule.getPriority());

        modelMapper.updateEntity(entity, rule);
        entity.setPriority(finalPriority);

        org.geoserver.acl.jpa.model.Rule saved = jparepo.save(entity);

        notifyCollateralUpdates(priorityResolver.getUpdatedIds());
        return modelMapper.toModel(saved);
    }

    private void removeLayerDetailsIfNotApplicableAnyMore(
            Rule rule, org.geoserver.acl.jpa.model.Rule entity) {
        if (entity.getLayerDetails() != null && !entity.getLayerDetails().isEmpty()) {
            boolean updatedCanHaveDetails =
                    ALLOW == rule.getIdentifier().getAccess()
                            && null != rule.getIdentifier().getLayer();
            if (!updatedCanHaveDetails) {
                log.info(
                        "Removing LayerDetails for Rule {} (entity id  {})."
                                + " Tansitioned from [access={}, layer={}] to [access={}, layer={}]",
                        rule.getId(),
                        entity.getId(),
                        entity.getIdentifier().getAccess(),
                        entity.getIdentifier().getLayer(),
                        rule.getIdentifier().getAccess(),
                        rule.getIdentifier().getLayer());
                entity.setLayerDetails(null);
            }
        }
    }

    @Override
    @TransactionRequired
    public Rule create(@NonNull Rule rule, @NonNull InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("Rule must have no id");
        if (rule.getPriority() < 0)
            throw new IllegalArgumentException(
                    "Negative priority is not allowed: " + rule.getPriority());

        findDup(rule).ifPresent(this::throwConflict);

        PriorityResolver<org.geoserver.acl.jpa.model.Rule> priorityResolver = priorityResolver();
        final long finalPriority =
                priorityResolver.resolveFinalPriority(rule.getPriority(), position);

        org.geoserver.acl.jpa.model.Rule entity = modelMapper.toEntity(rule);
        entity.setPriority(finalPriority);

        org.geoserver.acl.jpa.model.Rule saved = jparepo.save(entity);

        notifyCollateralUpdates(priorityResolver.getUpdatedIds());

        return modelMapper.toModel(saved);
    }

    // send an updated event for all collaterally updated rule
    private void notifyCollateralUpdates(Set<Long> ids) {
        if (!ids.isEmpty()) {
            Set<String> updatedIds =
                    ids.stream().map(RuleJpaMapper::encodeId).collect(Collectors.toSet());
            this.eventPublisher.accept(RuleEvent.updated(updatedIds));
        }
    }

    private Optional<Rule> findDup(Rule rule) {
        if (rule.getIdentifier().getAccess() == LIMIT) {
            return Optional.empty();
        }

        final Long id = decodeId(rule.getId());
        final RuleIdentifier identifier = modelMapper.toEntity(rule.getIdentifier());

        List<org.geoserver.acl.jpa.model.Rule> matches = jparepo.findAllByIdentifier(identifier);
        return matches.stream()
                .filter(r -> !r.getId().equals(id))
                .findFirst()
                .map(modelMapper::toModel);
    }

    @Override
    @TransactionRequired
    public boolean deleteById(@NonNull String id) {
        return 1 == jparepo.deleteById(decodeId(id).longValue());
    }

    @Override
    public boolean existsById(@NonNull String id) {
        return jparepo.existsById(decodeId(id));
    }

    @Override
    @TransactionRequired
    public int shift(long priorityStart, long offset) {
        if (offset <= 0) {
            throw new IllegalArgumentException("Positive offset required");
        }
        Set<Long> shiftedIds =
                jparepo.streamIdsByShiftPriority(priorityStart).collect(Collectors.toSet());
        if (shiftedIds.isEmpty()) {
            return -1;
        }
        int affectedCount = jparepo.shiftPriority(priorityStart, offset);
        notifyCollateralUpdates(shiftedIds);
        return affectedCount > 0 ? affectedCount : -1;
    }

    @Override
    @TransactionRequired
    public void swap(String id1, String id2) {

        org.geoserver.acl.jpa.model.Rule rule1 = getOrThrowIAE(id1);
        org.geoserver.acl.jpa.model.Rule rule2 = getOrThrowIAE(id2);

        long p1 = rule1.getPriority();
        long p2 = rule2.getPriority();

        rule1.setPriority(p2);
        rule2.setPriority(p1);

        jparepo.saveAll(List.of(rule1, rule2));
    }

    @Override
    @TransactionRequired
    public void setAllowedStyles(@NonNull String ruleId, Set<String> styles) {

        org.geoserver.acl.jpa.model.Rule rule = getOrThrowIAE(ruleId);

        if (RuleIdentifier.ANY.equals(rule.getIdentifier().getLayer())) {
            throw new IllegalArgumentException("Rule has no layer, can't set allowed styles");
        }
        if (rule.getLayerDetails() == null || rule.getLayerDetails().isEmpty()) {
            throw new IllegalArgumentException("Rule has no details associated");
        }

        LayerDetails layerDetails = rule.getLayerDetails();
        layerDetails.getAllowedStyles().clear();
        if (styles != null && !styles.isEmpty()) {
            layerDetails.getAllowedStyles().addAll(styles);
        }
        jparepo.save(rule);
    }

    @Override
    @TransactionRequired
    public void setLimits(String ruleId, RuleLimits limits) {
        org.geoserver.acl.jpa.model.Rule rule = getOrThrowIAE(ruleId);
        if (limits != null && rule.getIdentifier().getAccess() != GrantType.LIMIT) {
            throw new IllegalArgumentException("Rule is not of LIMIT type");
        }

        rule.setRuleLimits(modelMapper.toEntity(limits));

        jparepo.save(rule);
    }

    @Override
    @TransactionRequired
    public void setLayerDetails(
            String ruleId, org.geoserver.acl.domain.rules.LayerDetails detailsNew) {

        org.geoserver.acl.jpa.model.Rule rule = getOrThrowIAE(ruleId);

        if (rule.getIdentifier().getAccess() != GrantType.ALLOW && detailsNew != null)
            throw new IllegalArgumentException("Rule is not of ALLOW type");

        if (RuleIdentifier.ANY.equals(rule.getIdentifier().getLayer()) && detailsNew != null)
            throw new IllegalArgumentException("Rule does not refer to a fixed layer");

        LayerDetails details = modelMapper.toEntity(detailsNew);
        rule.setLayerDetails(details);
        jparepo.save(rule);
    }

    @Override
    @TransactionReadOnly
    public Optional<org.geoserver.acl.domain.rules.LayerDetails> findLayerDetailsByRuleId(
            @NonNull String ruleId) {

        org.geoserver.acl.jpa.model.Rule jparule = getOrThrowIAE(ruleId);

        // if (RuleIdentifier.ANY.equals(jparule.getIdentifier().getLayer())) {
        // throw new IllegalArgumentException("Rule " + ruleId + " has not layer set");
        // }

        LayerDetails jpadetails = jparule.getLayerDetails();
        if (jpadetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(modelMapper.toModel(jpadetails));
    }

    private org.geoserver.acl.jpa.model.Rule getOrThrowIAE(@NonNull String ruleId) {
        org.geoserver.acl.jpa.model.Rule rule;
        try {
            rule = jparepo.getReferenceById(decodeId(ruleId));
            rule.getIdentifier().getLayer();
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Rule " + ruleId + " does not exist");
        }
        return rule;
    }

    private void throwConflict(Rule dup) {
        throw new RuleIdentifierConflictException(
                "A Rule with the same identifier already exists: " + dup.toShortString());
    }
}

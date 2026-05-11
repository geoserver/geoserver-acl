/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.adaptor;

import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.LIMIT;
import static org.geoserver.acl.persistence.jpa.adaptor.RuleJpaMapper.decodeId;

import com.querydsl.core.CloseableIterator;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
import org.geoserver.acl.persistence.jpa.domain.JpaGrantType;
import org.geoserver.acl.persistence.jpa.domain.JpaLayerDetails;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleIdentifier;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleRepository;
import org.geoserver.acl.persistence.jpa.domain.QJpaRule;
import org.geoserver.acl.persistence.jpa.domain.TransactionReadOnly;
import org.geoserver.acl.persistence.jpa.domain.TransactionRequired;
import org.geoserver.acl.persistence.jpa.domain.TransactionSupported;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

@Slf4j
@TransactionSupported
public class RuleRepositoryJpaAdaptor implements RuleRepository {

    private final EntityManager em;

    private final JpaRuleRepository jparepo;
    private final RuleJpaMapper modelMapper;
    private final PredicateMapper queryMapper;

    @Setter
    private @NonNull Consumer<RuleEvent> eventPublisher = r -> {
        // no-op
    };

    public RuleRepositoryJpaAdaptor(EntityManager em, JpaRuleRepository jparepo, RuleJpaMapper mapper) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(jparepo);
        Objects.requireNonNull(mapper);
        this.em = em;
        this.modelMapper = mapper;
        this.jparepo = jparepo;
        this.queryMapper = new PredicateMapper();
    }

    private static final long PRIORITY_LOCK_ID = "acl_rule_priority".hashCode();

    /** Acquires a transaction-scoped advisory lock to serialize priority modifications across instances. */
    private void lockPrioritiesForUpdate() {
        em.createNativeQuery("SELECT pg_advisory_xact_lock(:lockId)")
                .setParameter("lockId", PRIORITY_LOCK_ID)
                .getSingleResult();
    }

    private PriorityResolver<org.geoserver.acl.persistence.jpa.domain.JpaRule> priorityResolver() {
        return new PriorityResolver<>(jparepo, org.geoserver.acl.persistence.jpa.domain.JpaRule::getPriority);
    }

    @Override
    public Optional<Rule> findById(@NonNull String id) {
        return jparepo.findById(decodeId(id)).map(modelMapper::toModel);
    }

    @Override
    public Optional<Rule> findOneByPriority(long priority) {
        Predicate predicate = QJpaRule.jpaRule.priority.eq(priority);
        try {
            return jparepo.findOne(predicate).map(modelMapper::toModel);
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
    @TransactionReadOnly
    public Stream<Rule> findAll() {
        return findAll(RuleQuery.of());
    }

    @Override
    @TransactionReadOnly
    public Stream<Rule> findAll(@NonNull RuleQuery<RuleFilter> query) {

        Predicate predicate = queryMapper.toPredicate(query);
        final java.util.function.Predicate<? super Rule> postFilter = filterByAddress(query.getFilter());

        if (query.getNextId() != null) {
            Long nextId = decodeId(query.getNextId());
            predicate = QJpaRule.jpaRule.id.goe(nextId).and(predicate);
        }

        CloseableIterator<org.geoserver.acl.persistence.jpa.domain.JpaRule> iterator = query(predicate);

        try (Stream<org.geoserver.acl.persistence.jpa.domain.JpaRule> stream = stream(iterator)) {
            Stream<Rule> rules = stream.map(modelMapper::toModel).filter(postFilter);
            final Integer pageSize = query.getLimit();
            if (null != pageSize) {
                rules = rules.limit(query.getLimit());
            }
            return rules.toList().stream();
        }
    }

    private CloseableIterator<org.geoserver.acl.persistence.jpa.domain.JpaRule> query(Predicate predicate) {

        return new JPAQuery<org.geoserver.acl.persistence.jpa.domain.JpaRule>(em)
                .from(QJpaRule.jpaRule)
                .where(predicate)
                .orderBy(new OrderSpecifier<>(Order.ASC, QJpaRule.jpaRule.priority))
                .iterate();
    }

    private Stream<org.geoserver.acl.persistence.jpa.domain.JpaRule> stream(
            CloseableIterator<org.geoserver.acl.persistence.jpa.domain.JpaRule> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .onClose(iterator::close);
    }

    private java.util.function.Predicate<? super Rule> filterByAddress(Optional<RuleFilter> filter) {

        if (filter.isEmpty()) return r -> true;
        IPAddressRangeFilter ipFilter = filter.get().getSourceAddress();
        return rule -> ipFilter.test(rule.identifier().addressRange());
    }

    @Override
    @TransactionRequired
    public Rule save(Rule rule) {
        final String id = Objects.requireNonNull(rule.id(), "id");
        lockPrioritiesForUpdate();
        findDup(rule).ifPresent(this::throwConflict);

        org.geoserver.acl.persistence.jpa.domain.JpaRule entity = getOrThrowIAE(id);
        removeLayerDetailsIfNotApplicableAnyMore(rule, entity);

        PriorityResolver<org.geoserver.acl.persistence.jpa.domain.JpaRule> priorityResolver = priorityResolver();
        long finalPriority = priorityResolver.resolvePriorityUpdate(entity.getPriority(), rule.priority());

        modelMapper.updateEntity(entity, rule);
        entity.setPriority(finalPriority);

        org.geoserver.acl.persistence.jpa.domain.JpaRule saved = jparepo.save(entity);

        notifyCollateralUpdates(priorityResolver.getUpdatedIds());
        return modelMapper.toModel(saved);
    }

    private void removeLayerDetailsIfNotApplicableAnyMore(
            Rule rule, org.geoserver.acl.persistence.jpa.domain.JpaRule entity) {
        if (entity.getLayerDetails() != null && !entity.getLayerDetails().isEmpty()) {
            boolean updatedCanHaveDetails = ALLOW == rule.identifier().access()
                    && null != rule.identifier().layer();
            if (!updatedCanHaveDetails) {
                log.info(
                        "Removing LayerDetails for Rule {} (entity id  {})."
                                + " Tansitioned from [access={}, layer={}] to [access={}, layer={}]",
                        rule.id(),
                        entity.getId(),
                        entity.getIdentifier().getAccess(),
                        entity.getIdentifier().getLayer(),
                        rule.identifier().access(),
                        rule.identifier().layer());
                entity.setLayerDetails(null);
            }
        }
    }

    @Override
    @TransactionRequired
    public Rule create(@NonNull Rule rule, @NonNull InsertPosition position) {
        lockPrioritiesForUpdate();
        if (null != rule.id()) throw new IllegalArgumentException("Rule must have no id");
        if (rule.priority() < 0)
            throw new IllegalArgumentException("Negative priority is not allowed: " + rule.priority());

        findDup(rule).ifPresent(this::throwConflict);

        PriorityResolver<org.geoserver.acl.persistence.jpa.domain.JpaRule> priorityResolver = priorityResolver();
        final long finalPriority = priorityResolver.resolveFinalPriority(rule.priority(), position);

        org.geoserver.acl.persistence.jpa.domain.JpaRule entity = modelMapper.toEntity(rule);
        entity.setPriority(finalPriority);

        org.geoserver.acl.persistence.jpa.domain.JpaRule saved = jparepo.save(entity);

        notifyCollateralUpdates(priorityResolver.getUpdatedIds());

        return modelMapper.toModel(saved);
    }

    // send an updated event for all collaterally updated rule
    private void notifyCollateralUpdates(Set<Long> ids) {
        if (!ids.isEmpty()) {
            Set<String> updatedIds = ids.stream().map(RuleJpaMapper::encodeId).collect(Collectors.toSet());
            this.eventPublisher.accept(RuleEvent.updated(updatedIds));
        }
    }

    private Optional<Rule> findDup(Rule rule) {
        if (rule.identifier().access() == LIMIT) {
            return Optional.empty();
        }

        final Long id = decodeId(rule.id());
        final JpaRuleIdentifier identifier = modelMapper.toEntity(rule.identifier());

        List<org.geoserver.acl.persistence.jpa.domain.JpaRule> matches = jparepo.findAllByIdentifier(identifier);
        return matches.stream().filter(r -> !r.getId().equals(id)).findFirst().map(modelMapper::toModel);
    }

    @Override
    @TransactionRequired
    public boolean deleteById(@NonNull String id) {
        org.geoserver.acl.persistence.jpa.domain.JpaRule rule;
        try {
            rule = jparepo.getReferenceById(decodeId(id));
            JpaLayerDetails details = rule.getLayerDetails();
            if (details != null) {
                details.setAllowedStyles(null);
                details.setAttributes(null);
            }
        } catch (EntityNotFoundException e) {
            return false;
        }
        jparepo.delete(rule);
        return true;
    }

    @Override
    @TransactionRequired
    public int deleteAll() {
        int count = count();
        jparepo.deleteAll();
        return count;
    }

    @Override
    public boolean existsById(@NonNull String id) {
        return jparepo.existsById(decodeId(id));
    }

    @Override
    @TransactionRequired
    public int shift(long priorityStart, long offset) {
        lockPrioritiesForUpdate();
        if (offset <= 0) {
            throw new IllegalArgumentException("Positive offset required");
        }

        Set<Long> shiftedIds = jparepo.streamIdsByShiftPriority(priorityStart).collect(Collectors.toSet());
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
        lockPrioritiesForUpdate();
        org.geoserver.acl.persistence.jpa.domain.JpaRule rule1 = getOrThrowIAE(id1);
        org.geoserver.acl.persistence.jpa.domain.JpaRule rule2 = getOrThrowIAE(id2);

        jparepo.swapPriorities(rule1.getId(), rule1.getPriority(), rule2.getId(), rule2.getPriority());
    }

    @Override
    @TransactionRequired
    public void setAllowedStyles(@NonNull String ruleId, Set<String> styles) {

        org.geoserver.acl.persistence.jpa.domain.JpaRule rule = getOrThrowIAE(ruleId);

        if (JpaRuleIdentifier.ANY.equals(rule.getIdentifier().getLayer())) {
            throw new IllegalArgumentException("Rule has no layer, can't set allowed styles");
        }
        if (rule.getLayerDetails() == null || rule.getLayerDetails().isEmpty()) {
            throw new IllegalArgumentException("Rule has no details associated");
        }

        JpaLayerDetails layerDetails = rule.getLayerDetails();
        layerDetails.getAllowedStyles().clear();
        if (styles != null && !styles.isEmpty()) {
            layerDetails.getAllowedStyles().addAll(styles);
        }
        jparepo.save(rule);
    }

    @Override
    @TransactionRequired
    public void setLimits(String ruleId, RuleLimits limits) {
        org.geoserver.acl.persistence.jpa.domain.JpaRule rule = getOrThrowIAE(ruleId);
        if (limits != null && rule.getIdentifier().getAccess() != JpaGrantType.LIMIT) {
            throw new IllegalArgumentException("Rule is not of LIMIT type");
        }

        rule.setRuleLimits(modelMapper.toEntity(limits));

        jparepo.save(rule);
    }

    @Override
    @TransactionRequired
    public void setLayerDetails(String ruleId, org.geoserver.acl.domain.rules.LayerDetails detailsNew) {

        org.geoserver.acl.persistence.jpa.domain.JpaRule rule = getOrThrowIAE(ruleId);

        if (rule.getIdentifier().getAccess() != JpaGrantType.ALLOW && detailsNew != null)
            throw new IllegalArgumentException("Rule is not of ALLOW type");

        if (JpaRuleIdentifier.ANY.equals(rule.getIdentifier().getLayer()) && detailsNew != null)
            throw new IllegalArgumentException("Rule does not refer to a fixed layer");

        JpaLayerDetails details = modelMapper.toEntity(detailsNew);
        rule.setLayerDetails(details);
        jparepo.save(rule);
    }

    @Override
    @TransactionReadOnly
    public Optional<org.geoserver.acl.domain.rules.LayerDetails> findLayerDetailsByRuleId(@NonNull String ruleId) {

        org.geoserver.acl.persistence.jpa.domain.JpaRule jparule = getOrThrowIAE(ruleId);

        JpaLayerDetails jpadetails = jparule.getLayerDetails();
        if (jpadetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(modelMapper.toModel(jpadetails));
    }

    private org.geoserver.acl.persistence.jpa.domain.JpaRule getOrThrowIAE(@NonNull String ruleId) {
        org.geoserver.acl.persistence.jpa.domain.JpaRule rule;
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

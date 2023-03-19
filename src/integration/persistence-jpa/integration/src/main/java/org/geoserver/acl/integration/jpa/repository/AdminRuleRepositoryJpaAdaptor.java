/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.repository;

import static org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper.decodeId;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.NonNull;

import org.geoserver.acl.adminrules.AdminRuleIdentifierConflictException;
import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper;
import org.geoserver.acl.jpa.model.QAdminRule;
import org.geoserver.acl.jpa.model.QRule;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.TransactionReadOnly;
import org.geoserver.acl.jpa.repository.TransactionRequired;
import org.geoserver.acl.jpa.repository.TransactionSupported;
import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.filter.AdminRuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.model.rules.InsertPosition;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

@TransactionSupported
public class AdminRuleRepositoryJpaAdaptor implements AdminRuleRepository {

    private final EntityManager em;
    private final JpaAdminRuleRepository jparepo;
    private final AdminRuleJpaMapper modelMapper;
    private final PredicateMapper queryMapper;
    private final PriorityResolver<org.geoserver.acl.jpa.model.AdminRule> priorityResolver;

    public AdminRuleRepositoryJpaAdaptor(
            EntityManager em, JpaAdminRuleRepository jparepo, AdminRuleJpaMapper mapper) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(jparepo);
        Objects.requireNonNull(mapper);
        this.em = em;
        this.modelMapper = mapper;
        this.jparepo = jparepo;
        this.queryMapper = new PredicateMapper();
        this.priorityResolver =
                new PriorityResolver<>(jparepo, org.geoserver.acl.jpa.model.AdminRule::getPriority);
    }

    @Override
    @TransactionRequired
    public AdminRule create(AdminRule rule, InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("Rule must have no id");
        if (rule.getPriority() < 0)
            throw new IllegalArgumentException(
                    "Negative priority is not allowed: " + rule.getPriority());

        final long finalPriority =
                priorityResolver.resolveFinalPriority(rule.getPriority(), position);

        org.geoserver.acl.jpa.model.AdminRule entity = modelMapper.toEntity(rule);
        entity.setPriority(finalPriority);

        org.geoserver.acl.jpa.model.AdminRule saved;
        try {
            // gotta use saveAndFlush to catch the exception before the method returns and the tx is
            // committed
            jparepo.flush();
            saved = jparepo.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new AdminRuleIdentifierConflictException(
                    "An AdminRule with the same identifier already exists: " + rule.toShortString(),
                    e);
        }
        return modelMapper.toModel(saved);
    }

    @Override
    public Optional<AdminRule> findById(@NonNull String id) {
        return jparepo.findById(decodeId(id).longValue()).map(modelMapper::toModel);
    }

    @Override
    public int count() {
        return (int) jparepo.count();
    }

    @Override
    public int count(AdminRuleFilter filter) {
        Optional<? extends Predicate> predicate = queryMapper.toPredicate(filter);
        if (predicate.isEmpty()) return (int) jparepo.count(predicate.get());
        return (int) jparepo.count();
    }

    @Override
    public Optional<AdminRule> findOneByPriority(long priority) {
        Predicate predicate = QAdminRule.adminRule.priority.goe(priority);
        try {
            return jparepo.findOne(predicate).map(modelMapper::toModel);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new IllegalArgumentException("Filter matches more than one AdminRule", e);
        }
    }

    @Override
    public Optional<AdminRule> findFirst(AdminRuleFilter adminRuleFilter) {

        Optional<Predicate> predicate = queryMapper.toPredicate(adminRuleFilter);
        PageRequest first = PageRequest.of(0, 1);
        Page<org.geoserver.acl.jpa.model.AdminRule> found;
        if (predicate.isPresent()) {
            found = jparepo.findAllNaturalOrder(predicate.get(), first);
        } else {
            found = jparepo.findAllNaturalOrder(first);
        }
        List<org.geoserver.acl.jpa.model.AdminRule> contents = found.getContent();
        return Optional.ofNullable(contents.isEmpty() ? null : contents.get(0))
                .map(modelMapper::toModel);
    }

    @Override
    @TransactionRequired
    public AdminRule save(AdminRule rule) {
        Objects.requireNonNull(rule.getId());
        org.geoserver.acl.jpa.model.AdminRule entity = getOrThrowIAE(rule.getId());

        long finalPriority =
                priorityResolver.resolvePriorityUpdate(entity.getPriority(), rule.getPriority());

        modelMapper.updateEntity(entity, rule);
        entity.setPriority(finalPriority);

        try {
            jparepo.flush();
            org.geoserver.acl.jpa.model.AdminRule saved = jparepo.saveAndFlush(entity);
            return modelMapper.toModel(saved);
        } catch (DataIntegrityViolationException e) {
            throw new AdminRuleIdentifierConflictException(
                    "An AdminRule with the same identifier already exists: " + rule.toShortString(),
                    e);
        }
    }

    @Override
    public List<AdminRule> findAll() {
        return findAll(RuleQuery.of());
    }

    @Override
    public List<AdminRule> findAll(@NonNull AdminRuleFilter filter) {
        return findAll(RuleQuery.of(filter));
    }

    //    @Override
    public List<AdminRule> findAllOld(RuleQuery<AdminRuleFilter> query) {
        Predicate predicate = queryMapper.toPredicate(query);
        Pageable pageRequest = queryMapper.toPageable(query);

        // REVISIT: if filter contains a non-any address range filter, can't apply paging to the db
        // query
        Page<org.geoserver.acl.jpa.model.AdminRule> page;
        page = jparepo.findAllNaturalOrder(predicate, pageRequest);

        List<org.geoserver.acl.jpa.model.AdminRule> found = page.getContent();
        return found.stream()
                .map(modelMapper::toModel)
                .filter(filterByAddress(query.getFilter()))
                .collect(Collectors.toList());
    }

    @Override
    @TransactionReadOnly
    public List<AdminRule> findAll(@NonNull RuleQuery<AdminRuleFilter> query) {

        Predicate predicate = queryMapper.toPredicate(query);
        final java.util.function.Predicate<? super AdminRule> postFilter =
                filterByAddress(query.getFilter());

        if (query.getNextCursor() != null) {
            Long nextId = decodeId(query.getNextCursor());
            predicate = QRule.rule.id.goe(nextId).and(predicate);
        }

        CloseableIterator<org.geoserver.acl.jpa.model.AdminRule> iterator = query(predicate);

        try (Stream<org.geoserver.acl.jpa.model.AdminRule> stream = stream(iterator)) {
            Stream<AdminRule> rules = stream.map(modelMapper::toModel).filter(postFilter);
            if (null != query.getPageSize()) {
                rules = rules.limit(query.getPageSize());
            }
            return rules.collect(Collectors.toList());
        }
    }

    private CloseableIterator<org.geoserver.acl.jpa.model.AdminRule> query(Predicate predicate) {

        CloseableIterator<org.geoserver.acl.jpa.model.AdminRule> iterator =
                new JPAQuery<org.geoserver.acl.jpa.model.AdminRule>(em)
                        .from(QAdminRule.adminRule)
                        .where(predicate)
                        .orderBy(new OrderSpecifier<>(Order.ASC, QRule.rule.priority))
                        .iterate();
        return iterator;
    }

    private Stream<org.geoserver.acl.jpa.model.AdminRule> stream(
            CloseableIterator<org.geoserver.acl.jpa.model.AdminRule> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .onClose(iterator::close);
    }

    private java.util.function.Predicate<? super AdminRule> filterByAddress(
            Optional<AdminRuleFilter> filter) {
        if (filter.isEmpty()) return r -> true;
        IPAddressRangeFilter ipFilter = filter.get().getSourceAddress();

        return ipFilter.toIPAddressPredicate(r -> r.getIdentifier().getAddressRange());
    }

    @Override
    @TransactionRequired
    public int shiftPriority(long priorityStart, long offset) {
        return jparepo.shiftPriority(priorityStart, offset);
    }

    @Override
    @TransactionRequired
    public void swap(@NonNull String id1, @NonNull String id2) {
        org.geoserver.acl.jpa.model.AdminRule rule1 = getOrThrowIAE(id1);
        org.geoserver.acl.jpa.model.AdminRule rule2 = getOrThrowIAE(id2);

        long p1 = rule1.getPriority();
        long p2 = rule2.getPriority();

        rule1.setPriority(p2);
        rule2.setPriority(p1);

        jparepo.saveAll(List.of(rule1, rule2));
    }

    @Override
    @TransactionRequired
    public boolean deleteById(@NonNull String id) {
        return 1 == jparepo.deleteById(decodeId(id).longValue());
    }

    private org.geoserver.acl.jpa.model.AdminRule getOrThrowIAE(@NonNull String ruleId) {
        org.geoserver.acl.jpa.model.AdminRule rule;
        try {
            rule = jparepo.getReferenceById(decodeId(ruleId).longValue());
        } catch (EntityNotFoundException e) {
            throw new NoSuchElementException("AdminRule " + ruleId + " does not exist");
        }
        return rule;
    }
}

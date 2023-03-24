/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.accessrules.model;

import com.google.common.collect.Streams;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.plugin.web.accessrules.event.PublishedInfoChangeEvent;
import org.geoserver.acl.plugin.web.components.AbstractRuleEditModel;
import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFacade;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.util.CloseableIterator;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.Service;
import org.geoserver.web.GeoServerApplication;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Slf4j
@SuppressWarnings("serial")
public class DataAccessRuleEditModel extends AbstractRuleEditModel<MutableRule> {

    /**
     * @see #workSpaceNameChanged(String)
     * @see #layerNameChanged(String)
     */
    private final PublishedInfoDetachableModel publishedInfoModel =
            new PublishedInfoDetachableModel();

    public DataAccessRuleEditModel() {
        this(new MutableRule());
    }

    public DataAccessRuleEditModel(@NonNull MutableRule rule) {
        super(rule);
        final String ruleID = rule.getId();
        if (null != ruleID && rule.canHaveLayerDetails()) {
            adminService()
                    .getLayerDetails(ruleID)
                    .map(MutableLayerDetails::new)
                    .ifPresent(rule::setLayerDetails);
        }
        if (null != rule.getLayer()) {
            publishedInfoModel.setObject(rule.getWorkspace(), rule.getLayer());
            PublishedInfo info = publishedInfoModel.getObject();
            if (null != info) {
                updateModelFor(info);
            }
        }
    }

    public LayerDetailsEditModel layerDetails() {
        return new LayerDetailsEditModel(this);
    }

    public IModel<PublishedInfo> getPublishedInfoModel() {
        return publishedInfoModel;
    }

    @Override
    protected String getRoleName(MutableRule rule) {
        return rule.getRoleName();
    }

    @Override
    public void save() {
        final MutableRule modelRule = getModelObject();
        if (isIncludeInstanceName()) {
            String instanceName = getInstanceName();
            modelRule.setInstanceName(instanceName);
        } else {
            modelRule.setInstanceName(null);
        }

        RuleAdminService service = adminService();
        final Rule rule;
        if (null == modelRule.getId()) {
            Rule newRule = modelRule.toRule();
            rule = service.insert(newRule);
        } else {
            Rule current = loadDomainRule();
            Rule toUpdate = modelRule.toRule(current);
            // this also removes the LayerDetails if its no longer applicable (e.g. the
            // grant type is no longer ALLOW and/or there's no layer set)
            rule = service.update(toUpdate);
        }
        LayerDetails ld = modelRule.toLayerDetails();
        if (null != ld) {
            service.setLayerDetails(rule.getId(), ld);
        }
        getModel().setObject(new MutableRule(rule, ld));
    }

    public Rule loadDomainRule() {
        MutableRule modelRule = getModelObject();
        RuleAdminService service = adminService();
        Rule current =
                service.get(modelRule.getId())
                        .orElseThrow(() -> new IllegalStateException("The rule no longer exists"));
        return current;
    }

    private RuleAdminService adminService() {
        return GeoServerApplication.get().getBeanOfType(RuleAdminService.class);
    }

    @Override
    protected String getInstanceName(MutableRule rule) {
        return rule.getInstanceName();
    }

    public List<String> findServiceNames() {
        return Stream.concat(
                        KNOWN_SERVICES.keySet().stream(),
                        findServices().map(Service::getId).map(String::toUpperCase))
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns a sorted list of operation names in the specified {@link Service#getId() service
     * name}
     */
    public List<String> findOperationNames(String serviceName) {
        if (!StringUtils.hasText(serviceName)) return List.of();

        Stream<String> knownOps = KNOWN_SERVICES.getOrDefault(serviceName, List.of()).stream();

        return Stream.concat(
                        knownOps,
                        findServices()
                                .filter(s -> s.getId().equalsIgnoreCase(serviceName))
                                .findFirst()
                                .map(Service::getOperations)
                                .map(List::stream)
                                .orElseGet(Stream::empty))
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    public Iterator<String> getLayerChoices(@Nullable String input) {
        String workspace = getModelObject().getWorkspace();
        workspace = nonNull(workspace);
        final String test = nonNull(input);

        Stream<String> options;

        final Catalog rawCatalog = rawCatalog();
        if (StringUtils.hasText(workspace)) {
            String prefixedSearch = workspace + ":" + test;
            Filter filter = Predicates.contains("prefixedName", prefixedSearch);
            SortBy sortByName = Predicates.sortBy("name", true);

            // REVISIT: check if it's actually closed
            try (CloseableIterator<PublishedInfo> it =
                    rawCatalog.list(PublishedInfo.class, filter, 0, MAX_SUGGESTIONS, sortByName)) {
                options =
                        Streams.stream(it)
                                .filter(PublishedInfo::isAdvertised)
                                .filter(PublishedInfo::isEnabled)
                                .map(PublishedInfo::getName)
                                .limit(MAX_SUGGESTIONS)
                                .collect(Collectors.toList())
                                .stream();
            }
        } else {
            options =
                    rawCatalog.getLayerGroupsByWorkspace(CatalogFacade.NO_WORKSPACE).stream()
                            .map(LayerGroupInfo::getName)
                            .sorted()
                            .limit(MAX_SUGGESTIONS);
        }

        return options.iterator();
    }

    public Iterator<String> getSubfieldChoices(@Nullable String input) {
        final Pattern test = caseInsensitiveContains(input);
        return KNOWN_WPS_PROCESSES.stream()
                .filter(process -> inputMatches(test, process))
                .iterator();
    }

    public Iterator<String> getStyleChoices(@Nullable String input) {
        Catalog catalog = rawCatalog();
        final Pattern test = caseInsensitiveContains(input);
        Stream<StyleInfo> styles =
                catalog.getStylesByWorkspace(CatalogFacade.NO_WORKSPACE).stream();
        String workspace = getModel().getObject().getWorkspace();
        if (StringUtils.hasText(workspace)) {
            List<StyleInfo> stylesByWorkspace = catalog.getStylesByWorkspace(workspace);
            styles = Stream.concat(styles, stylesByWorkspace.stream());
        }
        return styles.parallel()
                .map(StyleInfo::prefixedName)
                .filter(name -> inputMatches(test, name))
                .sorted()
                .limit(MAX_SUGGESTIONS)
                .iterator();
    }

    private Stream<Service> findServices() {
        return GeoServerExtensions.extensions(Service.class).stream();
    }

    private GrantType getAccess() {
        return getModelObject().getAccess();
    }

    public boolean isShowCatalogMode() {
        GrantType access = getAccess();
        switch (access) {
            case ALLOW:
                return isLayerSelected();
            case LIMIT:
                return true;
            default:
                return false;
        }
    }

    public boolean isShowRuleLimits() {
        return getAccess() == GrantType.LIMIT;
    }

    private boolean isLayerSelected() {
        String layer = getModelObject().getLayer();
        boolean hasLayer = StringUtils.hasText(layer);
        return hasLayer;
    }

    public Optional<PublishedInfoChangeEvent> workSpaceNameChanged(
            String workspace, AjaxRequestTarget target) {
        String layer = getModelObject().getLayer();
        return updatePublishedInfo(workspace, layer, target);
    }

    public Optional<PublishedInfoChangeEvent> layerNameChanged(
            String layer, AjaxRequestTarget target) {
        String workspace = getModelObject().getWorkspace();
        return updatePublishedInfo(workspace, layer, target);
    }

    private Optional<PublishedInfoChangeEvent> updatePublishedInfo(
            String workspace, String layer, AjaxRequestTarget target) {
        {
            String currWs = publishedInfoModel.getWorkspace();
            String currLayer = publishedInfoModel.getLayer();
            if (Objects.equals(currWs, workspace) && Objects.equals(currLayer, layer)) {
                return Optional.empty();
            }
        }
        PublishedInfo info = publishedInfoModel.setObject(workspace, layer);
        updateModelFor(info);
        return Optional.of(
                new PublishedInfoChangeEvent(workspace, layer, Optional.ofNullable(info), target));
    }

    private void updateModelFor(PublishedInfo info) {
        updateLayerType(info);
        updateLayerAttributes(info);
    }

    private void updateLayerType(PublishedInfo info) {
        MutableRule object = getModelObject();
        MutableLayerDetails ld = object.getLayerDetails();
        if (null != ld) {
            ld.setLayerTypeFrom(info);
        }
    }

    private void updateLayerAttributes(PublishedInfo info) {
        MutableRule object = getModelObject();
        MutableLayerDetails ld = object.getLayerDetails();
        if (null == ld) {
            return;
        }
        List<MutableLayerAttribute> origAtts = ld.getAttributes();
        if (!origAtts.isEmpty()) {
            return;
        }
        List<AttributeTypeInfo> attributes = List.of();
        if (info instanceof LayerInfo) {
            LayerInfo li = (LayerInfo) info;
            if (li.getType() == PublishedType.VECTOR) {
                FeatureTypeInfo resource = (FeatureTypeInfo) li.getResource();
                try {
                    attributes = resource.attributes();
                } catch (IOException e) {
                    log.warn("Error getting layer attributes for " + info.prefixedName(), e);
                }
            }
        }
        List<MutableLayerAttribute> mapped =
                attributes.stream().map(MutableLayerAttribute::new).collect(Collectors.toList());
        ld.getAttributes().clear();
        ld.getAttributes().addAll(mapped);
    }

    private MutableRule getModelObject() {
        return getModel().getObject();
    }

    private static Map<String, List<String>> KNOWN_SERVICES =
            Map.of(
                    "WMS",
                    List.of(
                            "GetCapabilities",
                            "GetMap",
                            "DescribeLayer",
                            "GetFeatureInfo",
                            "GetLegendGraphic",
                            "GetStyles"),
                    "WFS",
                    List.of(
                            "GetCapabilities",
                            "GetFeature",
                            "DescribeFeatureType",
                            "LockFeature",
                            "GetFeatureWithLock",
                            "Transaction"),
                    "WCS",
                    List.of("GetCapabilities", "GetCoverage", "DescribeCoverage"),
                    "WPS",
                    List.of("GetCapabilities", "DescribeProcess", "Execute"));

    private static final List<String> KNOWN_WPS_PROCESSES =
            List.of(
                    "JTS:area",
                    "JTS:boundary",
                    "JTS:buffer",
                    "JTS:centroid",
                    "JTS:contains",
                    "JTS:convexHull",
                    "JTS:crosses",
                    "JTS:densify",
                    "JTS:difference",
                    "JTS:dimension",
                    "JTS:disjoint",
                    "JTS:distance",
                    "JTS:endPoint",
                    "JTS:envelope",
                    "JTS:equalsExact",
                    "JTS:equalsExactTolerance",
                    "JTS:exteriorRing",
                    "JTS:geometryType",
                    "JTS:getGeometryN",
                    "JTS:getX",
                    "JTS:getY",
                    "JTS:interiorPoint",
                    "JTS:interiorRingN",
                    "JTS:intersection",
                    "JTS:intersects",
                    "JTS:isClosed",
                    "JTS:isEmpty",
                    "JTS:isRing",
                    "JTS:isSimple",
                    "JTS:isValid",
                    "JTS:isWithinDistance",
                    "JTS:length",
                    "JTS:numGeometries",
                    "JTS:numInteriorRing",
                    "JTS:numPoints",
                    "JTS:overlaps",
                    "JTS:pointN",
                    "JTS:polygonize",
                    "JTS:relate",
                    "JTS:relatePattern",
                    "JTS:reproject",
                    "JTS:simplify",
                    "JTS:splitPolygon",
                    "JTS:startPoint",
                    "JTS:symDifference",
                    "JTS:touches",
                    "JTS:union",
                    "JTS:within",
                    "centerLine:centerLine",
                    "geo:area",
                    "geo:boundary",
                    "geo:buffer",
                    "geo:centroid",
                    "geo:contains",
                    "geo:convexHull",
                    "geo:crosses",
                    "geo:densify",
                    "geo:difference",
                    "geo:dimension",
                    "geo:disjoint",
                    "geo:distance",
                    "geo:endPoint",
                    "geo:envelope",
                    "geo:equalsExact",
                    "geo:equalsExactTolerance",
                    "geo:exteriorRing",
                    "geo:geometryType",
                    "geo:getGeometryN",
                    "geo:getX",
                    "geo:getY",
                    "geo:interiorPoint",
                    "geo:interiorRingN",
                    "geo:intersection",
                    "geo:intersects",
                    "geo:isClosed",
                    "geo:isEmpty",
                    "geo:isRing",
                    "geo:isSimple",
                    "geo:isValid",
                    "geo:isWithinDistance",
                    "geo:length",
                    "geo:numGeometries",
                    "geo:numInteriorRing",
                    "geo:numPoints",
                    "geo:overlaps",
                    "geo:pointN",
                    "geo:polygonize",
                    "geo:relate",
                    "geo:relatePattern",
                    "geo:reproject",
                    "geo:simplify",
                    "geo:splitPolygon",
                    "geo:startPoint",
                    "geo:symDifference",
                    "geo:touches",
                    "geo:union",
                    "geo:within",
                    "gs:AddCoverages",
                    "gs:Aggregate",
                    "gs:AreaGrid",
                    "gs:BarnesSurface",
                    "gs:Bounds",
                    "gs:BufferFeatureCollection",
                    "gs:Centroid",
                    "gs:Clip",
                    "gs:CollectGeometries",
                    "gs:Contour",
                    "gs:Count",
                    "gs:CropCoverage",
                    "gs:Feature",
                    "gs:GeorectifyCoverage",
                    "gs:GetFullCoverage",
                    "gs:Grid",
                    "gs:Heatmap",
                    "gs:Import",
                    "gs:InclusionFeatureCollection",
                    "gs:IntersectionFeatureCollection",
                    "gs:LRSGeocode",
                    "gs:LRSMeasure",
                    "gs:LRSSegment",
                    "gs:MultiplyCoverages",
                    "gs:Nearest",
                    "gs:PagedUnique",
                    "gs:PointBuffers",
                    "gs:PointStacker",
                    "gs:PolygonExtraction",
                    "gs:Query",
                    "gs:RangeLookup",
                    "gs:RasterAsPointCollection",
                    "gs:RasterZonalStatistics",
                    "gs:RectangularClip",
                    "gs:Reproject",
                    "gs:ReprojectGeometry",
                    "gs:ScaleCoverage",
                    "gs:Simplify",
                    "gs:Snap",
                    "gs:StoreCoverage",
                    "gs:StyleCoverage",
                    "gs:Transform",
                    "gs:UnionFeatureCollection",
                    "gs:Unique",
                    "gs:VectorZonalStatistics",
                    "gt:VectorToRaster",
                    "polygonlabelprocess:PolyLabeller",
                    "ras:AddCoverages",
                    "ras:Affine",
                    "ras:AreaGrid",
                    "ras:BandMerge",
                    "ras:BandSelect",
                    "ras:Contour",
                    "ras:CoverageClassStats",
                    "ras:CropCoverage",
                    "ras:Jiffle",
                    "ras:MultiplyCoverages",
                    "ras:NormalizeCoverage",
                    "ras:PolygonExtraction",
                    "ras:RangeLookup",
                    "ras:RasterAsPointCollection",
                    "ras:RasterZonalStatistics",
                    "ras:ScaleCoverage",
                    "ras:StyleCoverage",
                    "ras:TransparencyFill",
                    "skeltonize:centerLine",
                    "vec:Aggregate",
                    "vec:BarnesSurface",
                    "vec:Bounds",
                    "vec:BufferFeatureCollection",
                    "vec:Centroid",
                    "vec:ClassifyByRange",
                    "vec:Clip",
                    "vec:CollectGeometries",
                    "vec:Count",
                    "vec:Feature",
                    "vec:FeatureClassStats",
                    "vec:Grid",
                    "vec:Heatmap",
                    "vec:InclusionFeatureCollection",
                    "vec:IntersectionFeatureCollection",
                    "vec:LRSGeocode",
                    "vec:LRSMeasure",
                    "vec:LRSSegment",
                    "vec:Nearest",
                    "vec:PointBuffers",
                    "vec:PointStacker",
                    "vec:Query",
                    "vec:RectangularClip",
                    "vec:Reproject",
                    "vec:Simplify",
                    "vec:Snap",
                    "vec:Transform",
                    "vec:UnionFeatureCollection",
                    "vec:Unique",
                    "vec:VectorToRaster",
                    "vec:VectorZonalStatistics");
}

/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.accessrules.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.model.LoadableDetachableModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFacade;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.GeoServerApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
@SuppressWarnings("serial")
class PublishedInfoDetachableModel extends LoadableDetachableModel<PublishedInfo> {

    private String id;
    private @Getter String workspace;
    private @Getter String layer;
    private Class<? extends PublishedInfo> type;

    public PublishedInfoDetachableModel() {}

    public PublishedInfoDetachableModel(PublishedInfo info) {
        setObject(info);
    }

    public PublishedInfo setObject(String workspace, String layer) {
        this.workspace = workspace;
        this.layer = layer;
        this.id = null;
        this.type = null;
        PublishedInfo info = loadByName();
        setObject(info);
        return info;
    }

    @Override
    public void setObject(final PublishedInfo info) {
        super.setObject(info);
        if (null != info) {
            WorkspaceInfo ws;
            if (info instanceof LayerGroupInfo) {
                type = LayerGroupInfo.class;
                ws = ((LayerGroupInfo) info).getWorkspace();
            } else if (info instanceof LayerInfo) {
                type = LayerInfo.class;
                ws = ((LayerInfo) info).getResource().getStore().getWorkspace();
            } else {
                throw new IllegalArgumentException("unknown PublishedInfo type " + info);
            }
            id = info.getId();
            layer = info.getName();
            workspace = ws == null ? null : ws.getName();
        }
        log.info("Selected layer changed [ws:{}, layer:{}, id:{}]", workspace, layer, id);
    }

    @Override
    protected PublishedInfo load() {
        PublishedInfo info = getObject();
        if (null != info) return info;
        if (null == id) return loadByName();
        return loadByIdAndType();
    }

    private PublishedInfo loadByIdAndType() {
        Catalog catalog = getRawCatalog();
        if (LayerGroupInfo.class.equals(type)) {
            return catalog.getLayerGroup(id);
        }
        return catalog.getLayer(id);
    }

    private PublishedInfo loadByName() {
        PublishedInfo info = null;
        if (null == workspace && null != layer) {
            Catalog catalog = getRawCatalog();
            info = catalog.getLayerGroupByName(CatalogFacade.NO_WORKSPACE, layer);
        } else if (null != workspace && null != layer) {
            Catalog catalog = getRawCatalog();
            info = catalog.getLayerByName(workspace + ":" + layer);
            if (null == info) info = catalog.getLayerGroupByName(workspace, layer);
        }
        return info;
    }

    private Catalog getRawCatalog() {
        ApplicationContext context = GeoServerApplication.get().getApplicationContext();
        Catalog catalog = context.getBean("rawCatalog", Catalog.class);
        return catalog;
    }
}

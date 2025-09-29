/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import java.util.regex.Pattern;
import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.geoserver.acl.api.model.Geom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GeometryApiMapper {

    Pattern pattern = Pattern.compile("((SRID=(\\d+))\\s*;)?\\s*(MULTIPOLYGON.*)");

    static final ThreadLocal<Boolean> USE_WKB = ThreadLocal.withInitial(() -> true);

    static void setUseWkb(boolean useWkb) {
        USE_WKB.set(useWkb);
    }

    default Geom geometryToApi(Geometry<? extends Position> geom) {
        if (null == geom) return null;

        Geom apiValue = new Geom();
        final boolean wkb = USE_WKB.get().booleanValue();
        if (wkb) {
            apiValue.setWkb(Wkb.toWkb(geom).toByteArray());
        } else {
            apiValue.setWkt(geolatteToWKT(geom));
        }
        return apiValue;
    }

    default org.geolatte.geom.Geometry<? extends Position> apiToGeometry(Geom geom) {
        if (geom == null) return null;
        Geometry<?> geometry = geom.getWkb() != null ? wkbToGeometry(geom.getWkb()) : wktToGeometry(geom.getWkt());
        if (null != geometry && -1 == geometry.getSRID()) {
            Geographic2DCoordinateReferenceSystem wgs84 =
                    CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
            geometry = Geometry.forceToCrs(geometry, wgs84);
        }
        return geometry;
    }

    default org.geolatte.geom.MultiPolygon<? extends Position> apiToMultiPolygon(
            Geometry<? extends Position> geometry) {
        // Geometry<? extends Position> geometry = apiToGeometry(geom);
        if (geometry == null) return null;
        if (!(geometry instanceof org.geolatte.geom.MultiPolygon)) {
            throw new IllegalArgumentException(
                    "Expected MULTIPOLYGON, got " + geometry.getClass().getSimpleName());
        }
        return (org.geolatte.geom.MultiPolygon<? extends Position>) geometry;
    }

    default org.geolatte.geom.Geometry<? extends Position> wktToGeometry(String wkt) {
        return wkt == null ? null : Wkt.fromWkt(wkt);
    }

    default org.geolatte.geom.Geometry<? extends Position> wkbToGeometry(byte[] wkb) {
        return wkb == null ? null : Wkb.fromWkb(ByteBuffer.from(wkb));
    }

    default String geolatteToWKT(org.geolatte.geom.Geometry<? extends Position> geom) {
        if (null == geom) return null;
        // int srid = geom.getSRID();
        String wkt = Wkt.toWkt(geom); // already has SRID prefix, uses postgis EWKT dialect
        // if (0 == srid) srid = 4326;
        // return String.format("SRID=%d;%s", srid, wkt);
        return wkt;
    }
}

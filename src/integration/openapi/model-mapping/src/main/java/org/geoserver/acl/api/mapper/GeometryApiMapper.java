/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.Wkt;
import org.geoserver.acl.api.model.Geom;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapstruct.Mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring")
public interface GeometryApiMapper {

    Pattern pattern = Pattern.compile("((SRID=(\\d+))\\s*;)?\\s*(MULTIPOLYGON.*)");

    default Geom geometryToApi(Geometry<? extends Position> geom) {
        if (null == geom) return null;

        Geom apiValue = new Geom();
        apiValue.setWkb(Wkb.toWkb(geom).toByteArray());
        return apiValue;
    }

    default org.geolatte.geom.MultiPolygon<? extends Position> apiToGeometry(Geom geom) {
        if (geom == null) return null;
        return geom.getWkb() != null ? toGeometry(geom.getWkb()) : toGeometry(geom.getWkt());
    }

    default org.geolatte.geom.MultiPolygon<? extends Position> toGeometry(String wkt) {
        return wkt == null ? null : castMultiPolygon(Wkt.fromWkt(wkt));
    }

    default org.geolatte.geom.MultiPolygon<? extends Position> toGeometry(byte[] wkb) {
        return wkb == null ? null : castMultiPolygon(Wkb.fromWkb(ByteBuffer.from(wkb)));
    }

    default String jtsToWKT(MultiPolygon geom) {
        if (null == geom) return null;
        int srid = geom.getSRID();
        String wkt = geom.toText();
        if (0 == srid) srid = 4326;

        return String.format("SRID=%d;%s", srid, wkt);
    }

    default String geolatteToWKT(org.geolatte.geom.MultiPolygon<? extends Position> geom) {
        if (null == geom) return null;
        // int srid = geom.getSRID();
        String wkt = Wkt.toWkt(geom); // already has SRID prefix, uses postgis EWKT dialect
        // if (0 == srid) srid = 4326;
        // return String.format("SRID=%d;%s", srid, wkt);
        return wkt;
    }

    default MultiPolygon wktToJTS(String sridDelimitedWKT) {
        if (null == sridDelimitedWKT) return null;

        Matcher matcher = pattern.matcher(sridDelimitedWKT);
        if (!matcher.matches()) {
            // TODO: log and/or throw
            return null;
        }
        String ssrid = matcher.group(3);
        int srid = ssrid == null ? 4326 : Integer.valueOf(ssrid);
        String wkt = matcher.group(4);
        MultiPolygon geom;
        try {
            geom = (MultiPolygon) new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid geometry", e);
        }
        geom.setSRID(srid);
        return geom;
    }

    default <P extends Position> org.geolatte.geom.MultiPolygon<P> wktToGeolatteMultiPolygon(
            String sridDelimitedWKT) {
        if (null == sridDelimitedWKT) return null;

        Geometry<?> geometry = Wkt.fromWkt(sridDelimitedWKT);
        return castMultiPolygon(geometry);
    }

    @SuppressWarnings("unchecked")
    default <P extends Position> org.geolatte.geom.MultiPolygon<P> castMultiPolygon(
            Geometry<?> geometry) {
        if (!(geometry instanceof org.geolatte.geom.MultiPolygon)) {
            throw new IllegalArgumentException(
                    "Expected MULTIPOLYGON, got " + geometry.getClass().getSimpleName());
        }
        return (org.geolatte.geom.MultiPolygon<P>) geometry;
    }
}

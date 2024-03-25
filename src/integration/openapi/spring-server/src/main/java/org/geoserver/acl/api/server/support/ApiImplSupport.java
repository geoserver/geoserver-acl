/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.api.mapper.EnumsApiMapperImpl;
import org.geoserver.acl.api.mapper.GeometryApiMapper;
import org.geoserver.acl.api.mapper.RuleFilterApiMapper;
import org.geoserver.acl.api.model.AdminRuleFilter;
import org.geoserver.acl.api.model.InsertPosition;
import org.geoserver.acl.api.model.RuleFilter;
import org.geoserver.acl.api.server.support.RequestBodyBufferingServletFilter.RequestBodyBufferingServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class ApiImplSupport<D, T> {

    private final @NonNull NativeWebRequest nativeRequest;
    private final @NonNull Function<T, D> toApi;
    private final @NonNull Function<D, T> toModel;

    private final RuleFilterApiMapper filterMapper = new RuleFilterApiMapper();

    public T toModel(D dto) {
        return toModel.apply(dto);
    }

    public D toApi(T model) {
        return toApi.apply(model);
    }

    public InsertPosition toApi(org.geoserver.acl.domain.rules.InsertPosition position) {
        return new EnumsApiMapperImpl().map(position);
    }

    public org.geoserver.acl.domain.rules.InsertPosition toRulesModel(InsertPosition position) {
        return new EnumsApiMapperImpl().toRuleInsertPosition(position);
    }

    public org.geoserver.acl.domain.adminrules.InsertPosition toAdminRulesModel(
            InsertPosition position) {
        return new EnumsApiMapperImpl().toAdminRuleInsertPosition(position);
    }

    public org.geoserver.acl.domain.adminrules.AdminRuleFilter map(
            AdminRuleFilter adminRuleFilter) {
        return filterMapper.map(adminRuleFilter);
    }

    public org.geoserver.acl.domain.rules.RuleFilter map(RuleFilter filter) {
        return filterMapper.toModel(filter);
    }

    public T mergePatch(final T orig) {
        D merged;
        try {
            RequestBodyBufferingServletRequest bufferedRequest =
                    nativeRequest.getNativeRequest(RequestBodyBufferingServletRequest.class);
            Objects.requireNonNull(
                    bufferedRequest,
                    "Servlet Filter not set up, expected RequestBodyBufferingServletRequest");
            BufferedReader reader = bufferedRequest.getReader();

            D current = toApi.apply(orig);
            ObjectReader readerForUpdating = new ObjectMapper().readerForUpdating(current);
            merged = readerForUpdating.readValue(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return toModel.apply(merged);
    }

    public <R> ResponseEntity<R> error(HttpStatus code, String reason) {
        return ResponseEntity.status(code).header("X-Reason", reason).build();
    }

    public void setPreferredGeometryEncoding() {
        String acceptContentType = nativeRequest.getHeader("Accept");
        boolean useWkb = true;
        if (StringUtils.hasText(acceptContentType)) {
            try {
                String contentType = acceptContentType.split(",")[0];
                MediaType mediaType = MediaType.parseMediaType(contentType);
                useWkb = !MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
            } catch (Exception e) {
                useWkb = false;
            }
        } else {
            useWkb = false;
        }
        GeometryApiMapper.setUseWkb(useWkb);
    }
}

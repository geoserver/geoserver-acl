package org.geoserver.acl.model.filter;

import lombok.Value;

import java.util.List;

@Value
public class Cursor<T> {
    private List<T> contents;
    private String nextCursor;
}

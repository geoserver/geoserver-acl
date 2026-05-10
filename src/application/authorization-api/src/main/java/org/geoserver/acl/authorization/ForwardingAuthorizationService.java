/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.authorization;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Base class for {@link AuthorizationService} decorators that forward every call to a wrapped
 * delegate.
 *
 * <p>Subclasses extend this type to layer behavior such as caching or instrumentation on top of a
 * concrete authorization service, overriding only the methods whose behavior they need to change
 * and inheriting straight delegation for the rest.
 *
 * @since 2.0
 */
@RequiredArgsConstructor
public abstract class ForwardingAuthorizationService implements AuthorizationService {

    @NonNull
    @Delegate
    @Getter
    private final AuthorizationService delegate;
}

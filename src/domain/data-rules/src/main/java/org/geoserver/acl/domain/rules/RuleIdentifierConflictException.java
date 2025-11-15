/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import java.io.Serial;

public class RuleIdentifierConflictException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RuleIdentifierConflictException(String msg) {
        super(msg);
    }

    public RuleIdentifierConflictException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

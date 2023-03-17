/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.adminrules;

public class AdminRuleIdentifierConflictException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public AdminRuleIdentifierConflictException(String msg) {
        super(msg);
    }

    public AdminRuleIdentifierConflictException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

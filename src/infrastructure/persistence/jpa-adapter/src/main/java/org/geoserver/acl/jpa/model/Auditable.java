/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@MappedSuperclass
public abstract class Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 141481953116476081L;

    @CreatedBy
    @Column(updatable = false, nullable = true)
    private String createdBy;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(updatable = true, nullable = true)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(updatable = true, nullable = true)
    private LocalDateTime lastModifiedDate;
}

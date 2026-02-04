package org.geoserver.acl.webapi.v1.server;

import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:51:12.712048-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.17.0")
@Controller
@RequestMapping("${openapi.geoServerAccessControlListACL.base-path:/api}")
public class AuthorizationApiController implements AuthorizationApi {

    private final AuthorizationApiDelegate delegate;

    public AuthorizationApiController(@Autowired(required = false) AuthorizationApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new AuthorizationApiDelegate() {});
    }

    @Override
    public AuthorizationApiDelegate getDelegate() {
        return delegate;
    }
}
